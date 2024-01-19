CREATE TABLE main_man(
	id VARCHAR(500) PRIMARY KEY,
    location_id VARCHAR(500) NOT NULL,
    destination_id VARCHAR(500) NOT NULL,
    price double
);
-- Create a trigger to populate the combined_column
DELIMITER //
CREATE TRIGGER combine_ids
BEFORE INSERT ON main_man
FOR EACH ROW
BEGIN
    SET NEW.id = CONCAT(NEW.location_id, NEW.destination_id);
END;
//
DELIMITER ;
