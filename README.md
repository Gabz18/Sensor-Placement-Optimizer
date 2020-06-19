# Voronoi Diagram Generator and Optimization Algorithm

Java cli tool to draw Voronoi diagrams && An optimization Algorithm based on Lloyd Algorithm.

Reused this [code](https://github.com/serenaz/voronoi/tree/06196dccf4e8b3e117b0e8451562133dc86cba87) and added a 
workaround to handle sites having the same y coordinate.

The provided diagram may not be 100% accurate in case of two sites having the same y coordinate 

This library also prints all of voronoi cells points in stdout.

## Prerequisites

- JDK 1.8
- Python 3.8

## Build to jar

```
git clone https://github.com/Gabz18/voronoi-generator.git
cd voronoi-generator
mkdir classes
javac -d classes src/generator/*.java
cd ./classes && jar cfve ../VoronoiGenerator.jar generator.Main . && cd ../
```

## Voronoi Generator Usage 

Arguments :

- width : -w $doubleValue, Required
- height : -h $doubleValue, Required
- site : -s $xDoubleValue:$yDoubleValue, At least one
- Dont show diagram: --no-display
- Sensor coverage radius : -s $doubleValue

````
java -jar VoronoiGenerator.jar -h 500.0 -w 500.0 -s 45.3:250.3 -s 260:350 -s 400:149.6
````

## Optimization algorithm

1. Build the voronoi generator Jar
2. python python/voronoi_relaxation.py
