# AP-Project



# compile (from project root)
find src -name "*.java" > sources.txt
javac -cp "src:lib/*" -d out @sources.txt

# run importer
java -cp "out:lib/*" erp.tools.ImportUsersCsv ./auth_db.csv

# run
java -cp "out:lib/*:src/resources" erp.Main

# demo
Admin	admin1	admin@123
Instructor	inst1 (or sambuddho, payel, etc.)	inst@123
Student	stu1 / stu2	stud@123