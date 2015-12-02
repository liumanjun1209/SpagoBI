ALTER TABLE SBI_DATA_SOURCE ALTER COLUMN URL_CONNECTION TYPE VARCHAR(500); 

ALTER TABLE SBI_ALARM ALTER COLUMN LABEL TYPE VARCHAR(50);
ALTER TABLE SBI_ALARM ALTER COLUMN LABEL SET NOT NULL;
Create UNIQUE Index SBI_ALARM_label_unique  ON SBI_ALARM (LABEL );

Create UNIQUE Index SBI_ALARM_CONTACT_name_unique  ON SBI_ALARM_CONTACT (NAME );

Create UNIQUE Index SBI_KPI_code_unique  ON SBI_KPI (code );

Create UNIQUE Index SBI_KPI_MODEL_unique_kpi_model ON SBI_KPI_MODEL (KPI_MODEL_CD );

Create UNIQUE Index SBI_KPI_PERIOD_name_unique  ON SBI_KPI_PERIODICITY (name );

ALTER TABLE SBI_THRESHOLD ALTER COLUMN code TYPE VARCHAR(45); 
ALTER TABLE SBI_THRESHOLD ALTER COLUMN code set NOT NULL;
Create UNIQUE Index SBI_THRESHOLD_code_unique  ON SBI_THRESHOLD (code );

ALTER TABLE SBI_THRESHOLD_VALUE ALTER COLUMN label type VARCHAR(20);
ALTER TABLE SBI_THRESHOLD_VALUE ALTER COLUMN label set  NOT NULL;
Create UNIQUE Index SBI_THRESHOLD_VALUE_label_thId  ON SBI_THRESHOLD_VALUE (label, THRESHOLD_ID );

Create UNIQUE Index SBI_RESOURCES_unique_res_name  ON SBI_RESOURCES (RESOURCE_NAME );

ALTER TABLE SBI_KPI_MODEL ALTER COLUMN KPI_MODEL_CD type VARCHAR(40);
ALTER TABLE SBI_KPI_MODEL ALTER COLUMN KPI_MODEL_CD set NOT NULL;

ALTER TABLE SBI_KPI_PERIODICITY ALTER COLUMN name type VARCHAR(400) ;
ALTER TABLE SBI_KPI_PERIODICITY ALTER COLUMN name set NOT NULL;

ALTER TABLE SBI_RESOURCES ALTER COLUMN RESOURCE_NAME type VARCHAR(40) ;
ALTER TABLE SBI_RESOURCES ALTER COLUMN RESOURCE_NAME set NOT NULL;

Create table SBI_EXPORTERS (
	DOMAIN_ID INTEGER NOT NULL,
	ENGINE_ID INTEGER NOT NULL,
	DEFAULT_VALUE BOOLEAN,
 	CONSTRAINT XPKSBI_EXPORTERS
              PRIMARY KEY (ENGINE_ID, DOMAIN_ID)
) WITHOUT OIDS;

Alter table SBI_EXPORTERS add  Foreign Key (ENGINE_ID) references SBI_ENGINES (ENGINE_ID);
Alter table SBI_EXPORTERS add  Foreign Key (DOMAIN_ID) references SBI_DOMAINS (VALUE_ID);


/*adds record for exporter functionality*/ 
Insert into SBI_DOMAINS (VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS) values ('PDF','PDF','EXPORT_TYPE','Exporters type','Exporters type');
Insert into SBI_DOMAINS (VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS) values ('XLS','XLS','EXPORT_TYPE','Exporters type','Exporters type');
Insert into SBI_DOMAINS (VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS) values ('JPG','JPG','EXPORT_TYPE','Exporters type','Exporters type');
Insert into SBI_DOMAINS (VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS) values ('PPT','PPT','EXPORT_TYPE','Exporters type','Exporters type');

commit;