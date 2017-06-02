### Cheat Sheet for SQLITE commands


1.  Start SQLite   --  <font color=blue>__sqlite3 <filename>__</font>

2.  Show all tables  --  <font color=blue>**.tables**</font>

3. Show whole database as SQL --  <font color=blue>**.schema**</font>

4. Create new table example:

       CREATE TABLE users (id INTEGER PRIMARY KEY,login TEXT,pass TEXT);

   <font color=red> The ‘id’ column is here what you call autoincrement in MySQL – if you assign NULL to this column the value will be incremented by 1 from last value. </font>


5.  Valid columntypes:

 * TEXT
 * INTEGER
 * REAL
 * BLOB

6. Rename table example

       ALTER TABLE users RENAME TO client_users;

This renames table users to client_users.

7. SELECT example


       SELECT * FROM table_name WHERE column_name = value;

   Selects all rows in table table_name where column column_name is equal to value

8. UPDATE example

        UPDATE table_name SET column_name = update_value WHERE some_column = some_value;

    Updates column_name with the value update_value in table table_name, on row/s where some_column is equal to some_value


9.  DELETE example


        DELETE FROM table_name WHERE column_name < 4;

Deletes all rows from table_name where column_name is less than 4

10. INSERT example

        INSERT INTO table_name (column1, column4, column7) VALUES (value1, 'value2', value3);

Inserts a new row into table table_name with values value1 in column1, value2 in column4 and value3 in column7 (other columns in table are left null or with default values, if set)

11. Read sql from file

         sqlite3 my.db < filewithsql

This will read and execute the sql statements from the file _filewithsql_.