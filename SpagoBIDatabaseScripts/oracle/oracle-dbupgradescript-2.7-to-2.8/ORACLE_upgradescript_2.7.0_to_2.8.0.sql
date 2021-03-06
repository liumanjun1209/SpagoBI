alter table sbi_org_unit modify NAME varchar2(200)
/
CREATE TABLE SBI_GOAL (
  GOAL_ID       INTEGER NOT NULL,
  GRANT_ID      INTEGER NOT NULL,
  START_DATE    DATE NOT NULL,
  END_DATE      DATE NOT NULL,
  NAME          VARCHAR2(20) NOT NULL,
  LABEL          VARCHAR2(20),
  DESCRIPTION		VARCHAR2(1000),
  PRIMARY KEY (GOAL_ID)
)
/

CREATE TABLE SBI_GOAL_HIERARCHY (
  GOAL_HIERARCHY_ID INTEGER NOT NULL,
  ORG_UNIT_ID       INTEGER NOT NULL,
  GOAL_ID           INTEGER NOT NULL,
  PARENT_ID         INTEGER,
  NAME              VARCHAR2(50) NOT NULL,
  LABEL             VARCHAR2(50),
  GOAL              VARCHAR2(1000),
  PRIMARY KEY (GOAL_HIERARCHY_ID)
)
/


CREATE TABLE SBI_GOAL_KPI (
  GOAL_KPI_ID         INTEGER NOT NULL,
  KPI_INSTANCE_ID     INTEGER NOT NULL,
  GOAL_HIERARCHY_ID   INTEGER NOT NULL,
  WEIGHT1             NUMBER(20,2),
  WEIGHT2             NUMBER(20,2),
  THRESHOLD1          NUMBER(20,2),
  THRESHOLD2          NUMBER(20,2),
  THRESHOLD1SIGN      INTEGER,
  THRESHOLD2SIGN      INTEGER,
  PRIMARY KEY (GOAL_KPI_ID)
)
/

ALTER TABLE SBI_GOAL ADD CONSTRAINT FK_GRANT_ID_GRANT FOREIGN KEY  (GRANT_ID) REFERENCES SBI_ORG_UNIT_GRANT (ID) ON DELETE CASCADE
/
                
ALTER TABLE SBI_GOAL_HIERARCHY ADD CONSTRAINT FK_SBI_GOAL_HIERARCHY_GOAL FOREIGN KEY  (GOAL_ID) REFERENCES SBI_GOAL (GOAL_ID) ON DELETE CASCADE
/
ALTER TABLE SBI_GOAL_HIERARCHY ADD CONSTRAINT FK_SBI_GOAL_HIERARCHY_PARENT FOREIGN KEY  (PARENT_ID) REFERENCES SBI_GOAL_HIERARCHY (GOAL_HIERARCHY_ID) ON DELETE CASCADE
/
 
ALTER TABLE SBI_GOAL_KPI ADD CONSTRAINT FK_SBI_GOAL_KPI_GOAL FOREIGN KEY  (GOAL_HIERARCHY_ID) REFERENCES SBI_GOAL_HIERARCHY (GOAL_HIERARCHY_ID)  ON DELETE CASCADE
/
ALTER TABLE SBI_GOAL_KPI ADD CONSTRAINT FK_SBI_GOAL_KPI_KPI FOREIGN KEY  (KPI_INSTANCE_ID) REFERENCES SBI_KPI_MODEL_INST (KPI_MODEL_INST) ON DELETE CASCADE
/


CREATE SEQUENCE SBI_GOAL_SEQ 
INCREMENT BY 1 
START WITH 1 
NOMAXVALUE 
NOMINVALUE 
NOCACHE  
NOCYCLE
NOORDER
/

CREATE SEQUENCE SBI_GOAL_HIERARCHY_SEQ 
INCREMENT BY 1 
START WITH 1 
NOMAXVALUE 
NOMINVALUE 
NOCACHE  
NOCYCLE
NOORDER
/                

CREATE SEQUENCE SBI_GOAL_KPI_SEQ 
INCREMENT BY 1 
START WITH 1 
NOMAXVALUE 
NOMINVALUE 
NOCACHE  
NOCYCLE
NOORDER
/      


create trigger TRG_SBI_GOAL
  BEFORE INSERT
  on SBI_GOAL
  REFERENCING OLD AS old NEW AS new
  for each row
  declare nuovo_id number;
begin
IF :new.GOAL_ID IS NULL THEN
     select SBI_GOAL_SEQ.nextval into nuovo_id from dual;
     :new.GOAL_ID:=nuovo_id;
END IF;
end;
/
create trigger TRG_SBI_GOAL_HIERARCHY
  BEFORE INSERT
  on SBI_GOAL_HIERARCHY
  REFERENCING OLD AS old NEW AS new
  for each row
  declare nuovo_id number;
begin
IF :new.GOAL_HIERARCHY_ID IS NULL THEN
     select SBI_GOAL_HIERARCHY_SEQ.nextval into nuovo_id from dual;
     :new.GOAL_HIERARCHY_ID:=nuovo_id;
END IF;
end;
/
create trigger TRG_SBI_GOAL_KPI
  BEFORE INSERT
  on SBI_GOAL_KPI
  REFERENCING OLD AS old NEW AS new
  for each row
  declare nuovo_id number;
begin
IF :new.GOAL_KPI_ID IS NULL THEN
     select SBI_GOAL_KPI_SEQ.nextval into nuovo_id from dual;
     :new.GOAL_KPI_ID:=nuovo_id;
END IF;
end;
/          