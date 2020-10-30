ALTER TABLE eg_pt_property
DROP COLUMN constructionyear;

ALTER TABLE eg_pt_property
ADD COLUMN constructionyear VARCHAR(255);


ALTER TABLE eg_pt_address
ADD COLUMN taxward VARCHAR(255);
ALTER TABLE eg_pt_address
ADD COLUMN wardno VARCHAR(255);
ALTER TABLE eg_pt_address
ADD COLUMN zone VARCHAR(255);
ALTER TABLE eg_pt_address
ADD COLUMN wardname VARCHAR(255);



CREATE TABLE IF NOT EXISTS eg_pt_property_payment (
    id VARCHAR (256) NOT NULL,
    propertyid VARCHAR (256) NOT NULL,
    financialyear VARCHAR (256),
    twelvepercentarv VARCHAR (256),
    arrearhousetax NUMERIC(10, 2),
    arrearwatertax NUMERIC(10, 2),
    arrearsewertax NUMERIC(10, 2),
    housetax NUMERIC(10, 2),
    watertax NUMERIC(10, 2),
    sewertax NUMERIC(10, 2),
    surcharehousetax NUMERIC(10, 2),
    surcharewatertax NUMERIC(10, 2),
    surcharesewertax NUMERIC(10, 2),
    billgeneratedtotal NUMERIC(10, 2),
    totalpaidamount NUMERIC(10, 2),
    lastpaymentdate VARCHAR (256),
    CONSTRAINT pk_eg_pt_property_payment_id PRIMARY KEY(id)
);

CREATE INDEX IF NOT EXISTS index_eg_pt_property_payment_propertyid	 ON eg_pt_property_payment (propertyid);