# AP-Project



# compile (from project root)
find src -name "*.java" > sources.txt
javac -cp "src:lib/*" -d out @sources.txt

# in cmd.exe
mkdir bin/del files.txt
for /R src %f in (*.java) do @echo %f >> files.txt
javac -cp ".;lib/*" -d bin @files.txt
java -cp "bin;lib/*" erp.ui.auth.LoginPage

# run importer
java -cp "out:lib/*" erp.tools.ImportUsersCsv ./auth_db.csv

# run
java -cp "out:lib/*:src/resources" erp.Main

# demo
Admin	admin1	admin@123
Instructor	inst1 (or sambuddho, payel, etc.)	inst@123
Student	stu1 / stu2	stud@123