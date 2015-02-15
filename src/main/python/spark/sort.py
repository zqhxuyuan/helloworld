from pyspark import SparkContext

sc = SparkContext("spark://server1:8888", "Python Sort", pyFiles=['cond.py'])
data = sc.textFile("hdfs://server1:9000/user/cc/reduced/")

print data.filter(lambda line : len(line.split('\t')) == 5).map(lambda line : (line.split('\t')[1],1)).reduceByKey(lambda x , y : x + y ).map(lambda pair : (pair[1],pair[0])).sortByKey(False).map(lambda pair : (pair[1],pair[0])).take(10) #.saveAsTextFile("hdfs://server1:9000/result")

