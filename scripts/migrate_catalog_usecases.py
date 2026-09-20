#!/usr/bin/env python3
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "catalog-application/src/main/java/com/catalog/application"
STORE_CMD = ROOT / "store/src/main/java/com/grab/store/catalog/internal/command/handler"
STORE_QRY = ROOT / "store/src/main/java/com/grab/store/catalog/internal/query/handler"
INBOUND = APP / "port/inbound"
SERVICE = APP / "service"

INBOUND.mkdir(parents=True, exist_ok=True)
STORE_CMD.mkdir(parents=True, exist_ok=True)
STORE_QRY.mkdir(parents=True, exist_ok=True)


def handler_to_base(name: str) -> str:
    if name.endswith("CommandHandler"):
        return name[: -len("CommandHandler")]
    if name.endswith("QueryHandler"):
        return name[: -len("QueryHandler")]
    raise ValueError(name)


def parse_handler(content: str):
    m = re.search(
        r"implements\s+CommandHandler<([^,]+),\s*([^>]+)>|implements\s+QueryHandler<([^,]+),\s*([^>]+)>",
        content,
    )
    if not m:
        return None
    if m.group(1):
        return "command", m.group(1).strip(), m.group(2).strip()
    return "query", m.group(3).strip(), m.group(4).strip()


def write_usecase(base: str, input_type: str, result_type: str, param: str):
    if input_type.startswith("com."):
        imp = f"import {input_type};"
        simple = input_type.split(".")[-1]
    else:
        pkg = "command" if "Command" in input_type else "query"
        imp = f"import com.catalog.application.{pkg}.{input_type};"
        simple = input_type
    (INBOUND / f"{base}UseCase.java").write_text(
        f"""package com.catalog.application.port.inbound;

{imp}

public interface {base}UseCase {{
    {result_type} execute({simple} {param});
}}
""",
        encoding="utf-8",
    )


def to_service(content: str, service: str, base: str) -> str:
    content = re.sub(
        r"package com\.catalog\.application\.(command|query)\.handler;",
        "package com.catalog.application.service;",
        content,
    )
    for pat in [
        r"import com\.catalog\.application\.config\.Catalog(?:Read)?Transactional;\n",
        r"import com\.grab\.framework\.cqrs\.(?:command|query)\.(?:Command|Query)Handler;\n",
        r"import org\.springframework\.stereotype\.Component;\n",
        r"import org\.springframework\.util\.StringUtils;\n",
        r"@Component\s*\n",
    ]:
        content = re.sub(pat, "", content)

    content = re.sub(
        r"public class \w+Handler\s*(?:\n\s*)?implements (?:Command|Query)Handler<[^>]+> \{",
        f"public class {service} implements {base}UseCase {{",
        content,
    )
    content = re.sub(r"Loggers\.getLogger\(\w+Handler\.class\)", f"Loggers.getLogger({service}.class)", content)
    content = re.sub(r"@Override\s*\n\s*@Catalog(?:Read)?Transactional\s*\n", "", content)
    content = re.sub(r"@Catalog(?:Read)?Transactional\s*\n", "", content)
    content = re.sub(r"public (\S+) handle\(", r"public \1 execute(", content)
    content = re.sub(
        r"\n\s*@Override\s*\n\s*public Class<[^>]+> get(?:Command|Query)Type\(\) \{\n\s*return [^;]+;\n\s*\}\n",
        "\n",
        content,
    )
    content = content.replace("StringUtils.hasText(", "hasText(")
    if "hasText(" in content and "private static boolean hasText" not in content:
        content = re.sub(r"\}\s*$", "", content.rstrip())
        content += """
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
"""
    if f"import com.catalog.application.port.inbound.{base}UseCase;" not in content:
        content = content.replace(
            "package com.catalog.application.service;",
            f"package com.catalog.application.service;\n\nimport com.catalog.application.port.inbound.{base}UseCase;",
            1,
        )
    has_ctor = re.search(rf"public {service}\s*\(", content)
    has_lombok = "@RequiredArgsConstructor" in content
    if not has_ctor and "private final " in content and not has_lombok:
        content = content.replace(
            f"public class {service} implements {base}UseCase",
            f"@lombok.RequiredArgsConstructor\npublic class {service} implements {base}UseCase",
        )
        if "import lombok.RequiredArgsConstructor;" not in content:
            content = content.replace(
                "package com.catalog.application.service;",
                "package com.catalog.application.service;\n\nimport lombok.RequiredArgsConstructor;",
                1,
            )
    elif has_lombok and "@RequiredArgsConstructor" in content:
        content = re.sub(r"@RequiredArgsConstructor\s*\n", "@lombok.RequiredArgsConstructor\n", content)
    return content


def resolve_import(simple_or_fq: str, kind: str) -> str:
    if simple_or_fq.startswith("com."):
        return simple_or_fq
    pkg = "command" if kind == "command" else "query"
    return f"com.catalog.application.{pkg}.{simple_or_fq}"


def result_imports(result_type: str, kind: str) -> list[str]:
    imports = []
    if result_type.startswith("Page<"):
        imports.append("org.springframework.data.domain.Page")
        inner = re.search(r"Page<(\w+)>", result_type)
        if inner:
            imports.append(resolve_import(inner.group(1), kind))
    elif result_type.startswith("com."):
        imports.append(result_type)
    else:
        imports.append(resolve_import(result_type, kind))
    return imports


def write_store_handler(kind: str, handler: str, base: str, input_type: str, result_type: str):
    pkg = (
        "com.grab.store.catalog.internal.command.handler"
        if kind == "command"
        else "com.grab.store.catalog.internal.query.handler"
    )
    input_fq = resolve_import(input_type, kind)
    simple_in = input_fq.split(".")[-1]
    tx = "CatalogTransactional" if kind == "command" else "CatalogReadTransactional"
    fw = "CommandHandler" if kind == "command" else "QueryHandler"
    fw_pkg = "command" if kind == "command" else "query"
    get_type = "getCommandType" if kind == "command" else "getQueryType"
    param = "command" if kind == "command" else "query"
    bean = base[0].lower() + base[1:] + "UseCase"

    imps = {input_fq, f"com.catalog.application.port.inbound.{base}UseCase", f"com.grab.framework.cqrs.{fw_pkg}.{fw}"}
    imps.add(f"com.grab.store.catalog.internal.config.{tx}")
    for ri in result_imports(result_type, kind):
        imps.add(ri)
    imp_block = "\n".join(f"import {i};" for i in sorted(imps))

    body = f"""package {pkg};

{imp_block}
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class {handler} implements {fw}<{simple_in}, {result_type}> {{

    private final {base}UseCase {bean};

    @Override
    @{tx}
    public {result_type} handle({simple_in} {param}) {{
        return {bean}.execute({param});
    }}

    @Override
    public Class<{simple_in}> {get_type}() {{
        return {simple_in}.class;
    }}
}}
"""
    dest = (STORE_CMD if kind == "command" else STORE_QRY) / f"{handler}.java"
    dest.write_text(body, encoding="utf-8")


def main():
    handlers = list((APP / "command/handler").glob("*.java")) + list(
        (APP / "query/handler").glob("*.java")
    )
    for path in sorted(handlers):
        content = path.read_text(encoding="utf-8")
        handler = path.stem
        base = handler_to_base(handler)
        service = base + "Service"
        parsed = parse_handler(content)
        if not parsed:
            print("SKIP", handler)
            continue
        kind, input_type, result_type = parsed
        param = "command" if kind == "command" else "query"
        write_usecase(base, input_type, result_type, param)
        (SERVICE / f"{service}.java").write_text(to_service(content, service, base), encoding="utf-8")
        write_store_handler(kind, handler, base, input_type, result_type)
        path.unlink()
    print("Done", len(handlers))


if __name__ == "__main__":
    main()
