-- Migration: Add missing product fields for external data import
-- This migration adds fields that are present in demo.json but missing from the products table

-- Add brand field
ALTER TABLE products ADD COLUMN brand VARCHAR(100);

-- Add tags field (as JSONB for better querying)
ALTER TABLE products ADD COLUMN tags JSONB;

-- Add discount_percentage field (original discount before sale_price calculation)
ALTER TABLE products ADD COLUMN discount_percentage DECIMAL(5,2);

-- Add warranty_information field
ALTER TABLE products ADD COLUMN warranty_information TEXT;

-- Add shipping_information field
ALTER TABLE products ADD COLUMN shipping_information TEXT;

-- Add return_policy field
ALTER TABLE products ADD COLUMN return_policy TEXT;

-- Add minimum_order_quantity field
ALTER TABLE products ADD COLUMN minimum_order_quantity INTEGER DEFAULT 1;

-- Add availability_status field
ALTER TABLE products ADD COLUMN availability_status VARCHAR(50) DEFAULT 'In Stock';

-- Add thumbnail field
ALTER TABLE products ADD COLUMN thumbnail VARCHAR(500);

-- Create indexes for better performance on new fields
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_tags ON products USING GIN(tags);
CREATE INDEX idx_products_discount_percentage ON products(discount_percentage);
CREATE INDEX idx_products_availability_status ON products(availability_status);

-- Add comments for documentation
COMMENT ON COLUMN products.brand IS 'Product brand name';
COMMENT ON COLUMN products.tags IS 'Array of product tags in JSON format';
COMMENT ON COLUMN products.discount_percentage IS 'Original discount percentage before sale price calculation';
COMMENT ON COLUMN products.warranty_information IS 'Product warranty details';
COMMENT ON COLUMN products.shipping_information IS 'Shipping information and policies';
COMMENT ON COLUMN products.return_policy IS 'Product return policy';
COMMENT ON COLUMN products.minimum_order_quantity IS 'Minimum quantity required for order';
COMMENT ON COLUMN products.availability_status IS 'Current availability status (In Stock, Low Stock, Out of Stock, etc.)';
COMMENT ON COLUMN products.thumbnail IS 'Thumbnail image URL for the product';

-- Update existing products to set default values
UPDATE products SET 
    minimum_order_quantity = 1,
    availability_status = CASE 
        WHEN stock_quantity > 0 THEN 'In Stock'
        ELSE 'Out of Stock'
    END
WHERE minimum_order_quantity IS NULL OR availability_status IS NULL;
