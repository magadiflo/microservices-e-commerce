INSERT INTO categories (name, description) VALUES
('Computadoras', 'Todo tipo de computadoras'),
('Laptops', 'Laptops de varias marcas'),
('Monitores', 'Monitores de diferentes tamaños y resoluciones'),
('Teclados', 'Teclados mecánicos y de membrana'),
('Mouse', 'Mouse para juegos y uso cotidiano'),
('Impresoras', 'Impresoras de tinta y láser'),
('Accesorios', 'Accesorios para computadoras'),
('Componentes', 'Componentes internos de computadoras'),
('Software', 'Software para productividad y juegos'),
('Redes', 'Equipos de redes y conectividad');

INSERT INTO products (name, description, available_quantity, price, category_id) VALUES
('Laptop Dell XPS 13', 'Laptop de alta gama de Dell', 50, 999.99, 2),
('Laptop HP Pavilion', 'Laptop para uso cotidiano', 100, 599.99, 2),
('Monitor Samsung 24"', 'Monitor Full HD de 24 pulgadas', 75, 149.99, 3),
('Monitor LG Ultrawide', 'Monitor Ultrawide para multitarea', 50, 299.99, 3),
('Teclado Mecánico Corsair', 'Teclado mecánico con retroiluminación RGB', 200, 129.99, 4),
('Teclado Logitech K120', 'Teclado básico para oficina', 300, 19.99, 4),
('Mouse Logitech G502', 'Mouse para juegos con alta precisión', 150, 49.99, 5),
('Mouse Microsoft Basic', 'Mouse básico para uso cotidiano', 250, 14.99, 5),
('Impresora HP LaserJet', 'Impresora láser monocromática', 50, 199.99, 6),
('Impresora Canon Pixma', 'Impresora de inyección de tinta a color', 80, 89.99, 6),
('Cable HDMI', 'Cable HDMI de alta velocidad', 500, 9.99, 7),
('Cargador de Laptop Universal', 'Cargador universal para laptops', 200, 29.99, 7),
('Procesador Intel i7', 'Procesador de alto rendimiento', 30, 299.99, 8),
('Tarjeta Gráfica NVIDIA GTX 1660', 'Tarjeta gráfica para juegos', 25, 229.99, 8),
('Software Microsoft Office', 'Suite de oficina de Microsoft', 100, 149.99, 9),
('Antivirus Norton', 'Software antivirus para protección', 200, 39.99, 9),
('Router TP-Link', 'Router inalámbrico de alta velocidad', 100, 49.99, 10),
('Switch Netgear', 'Switch de red de 8 puertos', 75, 29.99, 10),
('Laptop Lenovo ThinkPad', 'Laptop de negocios de Lenovo', 60, 899.99, 2),
('Monitor Acer 27"', 'Monitor de 27 pulgadas con resolución 4K', 40, 349.99, 3),
('Teclado Razer BlackWidow', 'Teclado mecánico para juegos', 100, 139.99, 4),
('Mouse Razer DeathAdder', 'Mouse para juegos con sensor óptico', 120, 59.99, 5),
('Impresora Epson EcoTank', 'Impresora de inyección de tinta con tanques recargables', 30, 249.99, 6),
('Auriculares Sony', 'Auriculares inalámbricos con cancelación de ruido', 150, 199.99, 7),
('Disco Duro Externo WD', 'Disco duro externo de 1TB', 200, 59.99, 7),
('Memoria RAM Corsair 16GB', 'Memoria RAM DDR4', 80, 79.99, 8),
('Placa Madre ASUS', 'Placa madre para procesadores Intel', 50, 129.99, 8),
('Sistema Operativo Windows 10', 'Sistema operativo de Microsoft', 150, 119.99, 9),
('Suite Adobe Creative Cloud', 'Suite de aplicaciones creativas de Adobe', 60, 239.99, 9),
('Extensor de Red TP-Link', 'Extensor de red inalámbrica', 100, 24.99, 10);