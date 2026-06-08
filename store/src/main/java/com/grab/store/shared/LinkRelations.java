package com.grab.store.shared;

import org.springframework.hateoas.LinkRelation;

public final class LinkRelations {

    public static final String SELF = "self";
    public static final String CATALOG = "catalog";
    public static final String INVENTORY = "inventory";

    public static final String PRODUCTS = "products";
    public static final String PRODUCT = "product";
    public static final String CATEGORIES = "categories";
    public static final String CATEGORY = "category";
    public static final String VARIANT_TYPES = "variant-types";
    public static final String VARIANT_OPTIONS = "variant-options";

    public static final String INVENTORIES = "inventories";
    public static final String LOCATIONS = "locations";
    public static final String ZONES = "zones";
    public static final String BINS = "bins";

    public static final String CREATE = "create";
    public static final String UPDATE = "update";
    public static final String ACTIVATE = "activate";
    public static final String DEACTIVATE = "deactivate";

    public static final String PARENT = "parent";
    public static final String CHILDREN = "children";
    public static final String TREE = "tree";
    public static final String COMBINATION = "combination";
    public static final String BUILD = "build";
    public static final String COMBINATIONS = "combinations";
    public static final String MOVEMENTS = "movements";
    public static final String RESERVATIONS = "reservations";

    private LinkRelations() {}
}
