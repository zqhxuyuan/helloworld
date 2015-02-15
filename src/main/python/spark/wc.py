from pyspark import SparkContext
from cond import isFirstMinute

sc = SparkContext("spark://server1:8888", "Python Analysis", pyFiles=['cond.py'])
data = sc.textFile("hdfs://server1:9000/user/cc/reduced/")
#fltData = data.filter(lambda line : line.split('\t')[0] < '00:01:00')
fltData = data.filter(lambda line : isFirstMinute(line))

print 'first minute  : ' + str(fltData.count())
