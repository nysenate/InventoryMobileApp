CREATE TABLE ad12verinv
( nuxrissue INTEGER PRIMARY KEY AUTOINCREMENT, nusenate TEXT NOT NULL,
 cdcond TEXT NOT NULL,  cdcategory TEXT NOT NULL, cdintransit TEXT NOT NULL,
   nuxrpickup INTEGER NOT NULL, decommodityf TEXT NOT NULL, cdlocatfrm TEXT NOT NULL,
 dttxnorigin TEXT NOT NULL, natxnorguser TEXT NOT NULL,
 dttxnupdate TEXT NOT NULL, natxnupduser TEXT NOT NULL );

CREATE TABLE ad12serial
( nuxrserial INTEGER PRIMARY KEY AUTOINCREMENT,  nusenate TEXT NOT NULL,
 nuserial TEXT NOT NULL,  decommodityf TEXT NOT NULL,
dttxnorigin TEXT NOT NULL, natxnorguser TEXT NOT NULL,
 dttxnupdate TEXT NOT NULL, natxnupduser TEXT NOT NULL)