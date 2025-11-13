# AP-Project

# macOS/ Linux
javac -d out -cp "lib/*" $(find src -name "*.java")
java -cp "out:lib/*:src" erp.Main

# Windows
javac -d out -cp "lib/*" (Get-ChildItem -Recurse src\*.java | ForEach-Object {$_.FullName})
java -cp "out;lib/*;src" erp.Main

# in cmd.exe ?
mkdir bin/del files.txt
for /R src %f in (*.java) do @echo %f >> files.txt
javac -cp ".;lib/*" -d bin @files.txt
xcopy src\resources bin\resources /E /I /Y   (this fixed the images not showing up)
java -cp "bin;lib/*" erp.ui.auth.LoginPage


# run importer
java -cp "out:lib/*" erp.tools.ImportUsersCsv ./auth_db.csv


### Demo Accounts

| Role          | Username                              | Password     |
|---------------|---------------------------------------|--------------|
| **Admin**     | `admin1`                              | `admin@123`  |
| **Instructor**| `inst1`, `sambuddho`, `payel`, etc.   | `inst@123`   |
| **Student**   | `stu1`, `stu2`                        | `stud@123`   |
