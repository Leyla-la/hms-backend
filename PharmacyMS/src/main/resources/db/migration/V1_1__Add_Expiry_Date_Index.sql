-- Enterprise Migration: Adding index for expiry stock scanning
-- This index prevents Full Table Scans when the background task scans for expired medicines.
-- Target Table: medicine_inventory
-- Target Column: expiry_date

USE pharmacydb;

ALTER TABLE medicine_inventory 
ADD INDEX idx_inventory_expiry (expiry_date);
