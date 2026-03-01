rm -r input
rm -r output
mkdir input
cp ../build/libs/demo-app-1.0.0.jar input
jpackage \
  --type app-image \
  --name MyApp \
  --input input \
  --main-jar demo-app-1.0.0.jar \
  --main-class com.nikitos.Main \
  --java-options '-Xmx512m' \
  --add-modules java.base,java.desktop,java.logging \
  --dest output


