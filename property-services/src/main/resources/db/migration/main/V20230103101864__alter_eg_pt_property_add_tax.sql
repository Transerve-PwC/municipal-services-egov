ALTER TABLE eg_pt_property
ADD water_tax  NUMERIC (10,2);

ALTER TABLE eg_pt_property
ADD house_tax  NUMERIC (10,2);

ALTER TABLE eg_pt_property
ADD sewer_tax  NUMERIC (10,2);

ALTER TABLE eg_pt_property
ADD property_id_ptms CHARACTER VARYING (256);