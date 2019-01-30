import sqlite3
fichierDonnees="Clavard.db"
conn=sqlite3.connect(fichierDonnees)
cur=conn.cursor()

cur.execute("CREATE TABLE messages (loginemetteur TEXT,loginrecepteur TEXT, date TEXT, mess TEXT)")

#cur.execute("select * from messages where (loginemetteur = 'Mehdi' and loginrecepteur ='Le Balayssac FR')or (loginemetteur = 'Le Balayssac FR' and loginrecepteur ='Mehdi') ").fetchall()


#cur.execute("INSERT INTO messages(loginemetteur,loginrecepteur,date,mess) VALUES('mehdi','antoine','13-4-2015 10:59:26','Ah !')")
#cur.execute("INSERT INTO messages(loginemetteur,loginrecepteur,date,mess) VALUES('antoine','mehdi','17-4-2015 10:59:26','Ah oui !')")


conn.commit()
cur.close()
conn.close()
