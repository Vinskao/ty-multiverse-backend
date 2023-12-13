-- 建立資料庫指令
-- CREATE DATABASE java COLLATE Chinese_Taiwan_Stroke_CI_AS; --

Use java
GO

DROP TABLE if exists detail;
DROP TABLE if exists  product;
CREATE TABLE product (
   id       integer primary key,
   name     nvarchar(20),
   price    float,
   make     datetime,
   expire    integer
);

INSERT INTO product values (1,'Coca Cola',20, '2007-01-01',365);
INSERT INTO product values (2,'Milk Tea',15, '2007-02-14',150);
INSERT INTO product values (3,'Easy Coffe',10, '2007-10-01',200);
INSERT INTO product values (4,'Coffe Square',15, '2007-02-20',100);
INSERT INTO product values (5,'Cookie',25, '2007-03-27',45);
INSERT INTO product values (6,'Prince Noodle',5, '2007-04-02',365);
INSERT INTO product values (7,'Chicken Noodle',20, '2006-10-30',300);
INSERT INTO product values (8,'Qwi-Qwi',20, '2007-02-28',200);
INSERT INTO product values (9,'Ice Pop',15, '2007-05-30',150);
INSERT INTO product values (10,'HotDog',25, '2007-04-30',1);

/*==========================================================================*/

CREATE TABLE detail (
   photoid  integer primary key REFERENCES product(id),
   photo    image
);

/*==========================================================================*/

DROP TABLE if exists  customer;
CREATE TABLE customer (
   custid     varchar(20) primary key,
   password   varbinary(50),
   email      nvarchar(30),
   birth      datetime
);

INSERT INTO customer values ('Alex', 0x41, 'alex@lab.com', '2001-07-20');
INSERT INTO customer values ('Babe', 0x42, 'babe@lab.com', '2003-03-20');
INSERT INTO customer values ('Carol', 0x43, 'carol@lab.com', '2001-09-11');
INSERT INTO customer values ('Dave', 0x44, 'dave@lab.com', '2001-01-20');
INSERT INTO customer values ('Ellen', 0x45, 'ellen@lab.com', '2000-05-20');

/*==========================================================================*/
DROP TABLE if exists projemp;
DROP TABLE if exists proj;
DROP TABLE if exists emp;
DROP TABLE if exists dept;

CREATE TABLE dept (
  deptid     integer  primary key,
  deptname   NVARCHAR(20)
);

INSERT INTO DEPT VALUES (10, 'Java');
INSERT INTO DEPT VALUES (20, 'Delphi');
INSERT INTO DEPT VALUES (30, 'Visual Basic');

/*=====================================================================*/

CREATE TABLE proj (
  projid    integer  primary key,
  projname  NVARCHAR(50)
);

INSERT INTO PROJ VALUES (100, 'Online Shopping');
INSERT INTO PROJ VALUES (200, 'Mobile Banking');

/*=====================================================================*/

CREATE TABLE emp (
  empid     integer primary key identity,
  empname   NVARCHAR(10),
  salary    integer,
  sex       CHAR(1),
  photo     image,
  deptid    integer NOT NULL REFERENCES DEPT(DEPTID)
);

INSERT INTO EMP (EMPNAME, SALARY, SEX, DEPTID) VALUES ('Samuel', 10, 'M', 10);
INSERT INTO EMP (EMPNAME, SALARY, SEX, DEPTID) VALUES ('Crystal', 100, 'F', 30);
INSERT INTO EMP (EMPNAME, SALARY, SEX, DEPTID) VALUES ('Sammy', 1000, 'M', 10);
INSERT INTO EMP (EMPNAME, SALARY, SEX, DEPTID) VALUES ('Momo', 10000, 'M', 20);

/*=====================================================================*/

CREATE TABLE projemp (
  projid  integer,
  empid   integer,
  CONSTRAINT PK_PROJEMP_PROJIDEMPID PRIMARY KEY(PROJID, EMPID),
  CONSTRAINT FK_PROJEMP_PROJ_PROJID FOREIGN KEY(PROJID) REFERENCES PROJ(PROJID),
  CONSTRAINT FK_PROJEMP_EMP_EMPID FOREIGN KEY(EMPID) REFERENCES EMP(EMPID)
);

INSERT INTO PROJEMP VALUES (100, 1);
INSERT INTO PROJEMP VALUES (100, 2);
INSERT INTO PROJEMP VALUES (100, 4);
INSERT INTO PROJEMP VALUES (200, 3);
INSERT INTO PROJEMP VALUES (200, 4);

/*=====================================================================*/
DROP PROCEDURE if exists proProductByPrice;

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE proProductByPrice
	@param_price float AS
BEGIN
	SET NOCOUNT ON;
	SELECT * FROM product WHERE price>@param_price
END
GO

/*==========================================================================*/
