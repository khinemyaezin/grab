insert into public.product (category_id, id, name, uuid)
values  (4, 1, 'Shirt', 'c9ac1163-e67e-4ea4-ad40-2eaf093d6b76');

insert into public.product_variant (id, product_id, sku, uuid)
values  (1, 1, 'SKU-1', '2c095488-b386-4861-8234-cb8896a401a1');

insert into public.product_variant_option (id, variant_id, variant_option_id, variant_type_id, uuid, variant_option_value, variant_type_value)
values  (1, 1, 3, 2, '4e744b3d-9bf4-4b87-ba5e-da0a14e5c8b0', 'Small', 'Size'),
        (2, 1, 1, 1, '2edc0294-6103-4205-89f8-9d207758a0fa', 'Yellow', 'Color');
