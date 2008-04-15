

-- Database and tables associated with AAA component, associated with users
-- and authentication.

CREATE DATABASE IF NOT EXISTS aaa;
USE aaa;

-- create empty users table 
-- use the tools/utils/idc-useradd script to add a first administrative user
-- after that use the WBUI

CREATE TABLE IF NOT EXISTS users (
    id                  INT NOT NULL AUTO_INCREMENT,
    login               TEXT NOT NULL,
    certIssuer          TEXT,
    certSubject         TEXT,
    lastName            TEXT NOT NULL,
    firstName           TEXT NOT NULL,
    emailPrimary        TEXT NOT NULL,
    phonePrimary        TEXT NOT NULL,
    password            TEXT,
    description         TEXT,
    emailSecondary      TEXT,
    phoneSecondary      TEXT,
    status              TEXT,
    activationKey       TEXT,
    loginTime           BIGINT,
    cookieHash          TEXT,
    institutionId       INT NOT NULL,    -- foreign key (when convert to InnoDB)
    PRIMARY KEY (id)
) type=MyISAM;

-- populate institutions table     

CREATE TABLE IF NOT EXISTS institutions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO institutions VALUES(1, "Energy Sciences Network");
INSERT INTO institutions VALUES(2, "Internet2");

-- populate resources table

CREATE TABLE IF NOT EXISTS resources (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO resources VALUES(NULL, "Users",
                        "Information about all users", NULL);
INSERT INTO resources VALUES(NULL, "Reservations",
                        "Information about all reservations", NULL);
INSERT INTO resources VALUES(NULL, "Domains",
                        "Information about OSCARS-realm domain controllers",
                        NULL);

-- populate attributes table

CREATE TABLE IF NOT EXISTS attributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    attrType            TEXT,
    PRIMARY KEY (id)
) type=MyISAM;

-- ordinary OSCARS user
INSERT INTO attributes VALUES(NULL, "OSCARS-user", "group");

-- member of the  network engineering group. Has complete control over
-- all reservations
INSERT INTO attributes VALUES(NULL, "OSCARS-engineer", "group");

-- Has complete control over all user accounts, including granting permissions
INSERT INTO attributes VALUES(NULL, "OSCARS-administrator", "group");

-- attribute for an IDC in an adjacent network domain. It's attributes implement
-- an SLA between domains.  Currently set to all permissions on reservations and 
-- query permissions for domains, no permissions on users
INSERT INTO attributes VALUES(NULL, "OSCARS-service", "group");

-- for use by NOC operators. Can see all reservations.
INSERT INTO attributes VALUES(NULL, "OSCARS-operator", "group");

-- populate userAttributes table by selecting attributes in tool/utils/idc-adduser

CREATE TABLE IF NOT EXISTS userAttributes (
    id                  INT NOT NULL AUTO_INCREMENT,
    userId              INT NOT NULL,    -- foreign key
    attributeId         INT NOT NULL,    -- foreign key
    PRIMARY KEY (id)
) type=MyISAM;
        
       
-- populate permissions table

CREATE TABLE IF NOT EXISTS permissions (
    id                  INT NOT NULL AUTO_INCREMENT,
    name                TEXT NOT NULL,
    description         TEXT,
    updateTime          BIGINT,
    PRIMARY KEY (id)
) type=MyISAM;

INSERT INTO permissions VALUES(NULL, "list",
            "view minimum information about a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "query",
            "view complete information about a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "modify",
            "change or delete a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "create",
            "create a user or reservation", NULL);
INSERT INTO permissions VALUES(NULL, "signal",
            "signal a previously placed reservation", NULL);


-- populate authorizations table
        
CREATE TABLE IF NOT EXISTS authorizations (
    id                  INT NOT NULL AUTO_INCREMENT,
    context             TEXT,
    updateTime          BIGINT,
    attrId              INT NOT NULL,    -- foreign key
    resourceId          INT NOT NULL,    -- foreign key
    permissionId        INT NOT NULL,    -- foreign key
    constraintName      TEXT,
    constraintValue     INT,
    PRIMARY KEY (id)
) type=MyISAM;

-- authorizations for standard attributes
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
    "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
    "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     NULL, NULL);   
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-user"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     NULL, NULL); 
     
-- authorizations for OSCARS-engineer
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
    "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
    "all-users", 0);
-- super-user authorizations for BSS operations
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "specify-path-elements", 1);  
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     "all_users", 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="domains"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-engineer"),
     (select id from resources where name="domains"),
     (select id from permissions where name="modify"),
     NULL, NULL);
     
--  Authorizations for OSCARS-administrator
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="list"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="create"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-administrator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
     "all-users", 1);
     
-- authorizations for service user 
-- note that all the reservations fowarded by a service are owned by the service
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="domains"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="modify"),
     NULL, NULL); 
-- list is only used in debugging interdomain interactions
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     NULL, NULL); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "specify-path-elements", 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="create"),
     "specify-gri", 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="signal"),
     NULL, NULL);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="domains"),
     (select id from permissions where name="query"),
     NULL, NULL);
 INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-service"),
     (select id from resources where name="domains"),
     (select id from permissions where name="modify"),
     NULL, NULL);
     
-- NOC operators
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="list"),
     "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="reservations"),
     (select id from permissions where name="query"),
     "all-users", 1); 
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="list"),
    "all-users", 1);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="query"),
    "all-users", 0);
INSERT INTO authorizations VALUES(NULL,NULL,NULL,
     (select id from attributes where name="OSCARS-operator"),
     (select id from resources where name="users"),
     (select id from permissions where name="modify"),
    "all-users", 0);     