INSERT INTO variant_type(code,name,uuid)
VALUES
    ('color','Color','b7922f9f-1fe9-4e51-9949-982a7398c92a'),
    ('size','Size','71269f58-4d53-4f08-8678-74487ad99217');

insert into variant_option(code,name,uuid,variant_type_id)
values ('yellow','Yellow','94f86eab-8be2-4cee-9303-3340728fcda7',1),
       ('blue','Blue','8578eda9-fc45-44fe-af5d-0ded9a9bc54d',1),
       ('small','Small','128cb594-740c-4735-89f3-c930d2af0b7e',2);