'''
Created on Sep 16, 2010
kNN: k Nearest Neighbors

Input:      inX: vector to compare to existing dataset (1xN)
            dataSet: size m data set of known vectors (NxM)
            labels: data set labels (1xM vector)
            k: number of neighbors to use for comparison (should be an odd number)
            
Output:     the most popular class label

@author: pbharrin
'''
from numpy import *
import operator
from os import listdir

import matplotlib
import matplotlib.pyplot as plt

# 对未知类别属性的数据集中的每个点依次执行如下操作:
# 1) 计算已知类别数据集中的点到当前点之间的距离
# 2) 按照距离递增的顺序排序
# 3) 选取与当前距离最小的k个点
# 4) 确定前k个点所在类别的出现频率
# 5) 返回k个点出现频率最搞的类别,作为当前点的预测分类
def testClassify0:
    group, labels = createDataSet()
    classify0([0,0], group, labels, 3)

# 参数说明: 用于分类的输入向量inX, 输入的训练样本集dataSet, 标签向量labels, 用于选择最近邻居的熟木k
def classify0(inX, dataSet, labels, k):
    # 1. 距离计算
    dataSetSize = dataSet.shape[0]
    diffMat = tile(inX, (dataSetSize,1)) - dataSet
    sqDiffMat = diffMat**2
    sqDistances = sqDiffMat.sum(axis=1)
    distances = sqDistances**0.5
    # 计算所有点之间的距离后, 对数据按照从小到大递增的顺序排序
    sortedDistIndicies = distances.argsort()     
    classCount={}  # 元组列表
    # 2. 选择距离最小的k个点: 确定前k个距离最小元素所在的主要(vote)分类          
    for i in range(k):
        # 前k个元素,每个元素都对应了所属的标签. classCount用来标记每种标签出现的次数.
        voteIlabel = labels[sortedDistIndicies[i]]
        # 如果标签没有出现过,初始值=0,并将classCount这个标签对应的次数+1.
        classCount[voteIlabel] = classCount.get(voteIlabel,0) + 1
        # 循环结束后,classCount里记录的是前k个元素,按照标签进行分类,对应出现的次数
    # 3. 排序
    # 迭代classCount的每个元素, 对其第二个元素(索引为1)进行降序(reverse)排序, 
    # 第二个元素为标签出现的次数, 所以sortedClassCount为按照标签出现的次数降序排列
    sortedClassCount = sorted(classCount.iteritems(), key=operator.itemgetter(1), reverse=True)
    return sortedClassCount[0][0]

def createDataSet():
    group = array([[1.0,1.1],[1.0,1.0],[0,0],[0,0.1]])
    labels = ['A','A','B','B']
    return group, labels

# 输入为文件(datingTestSet), 输出为训练样本矩阵和分类标签向量
# 输入数据示例: 飞行常客里程数, 玩游戏的时间比, 每周吃了多少冰淇淋, ==> 不喜欢1, 一般2, 很喜欢3
# 40920	8.326976	0.953952	largeDoses
# 14488	7.153469	1.673904	smallDoses
# 26052	1.441871	0.805124	didntLike
def file2matrix(filename):
    love_dictionary={'largeDoses':3, 'smallDoses':2, 'didntLike':1}
    fr = open(filename)
    arrayOLines = fr.readlines()
    numberOfLines = len(arrayOLines)     	#get the number of lines in the file 得到文件行数
    returnMat = zeros((numberOfLines,3))   	#prepare matrix to return 创建返回的NumPy矩阵
    classLabelVector = []                      	#prepare labels return 分类标签特征向量  
    index = 0
    for line in arrayOLines:
        line = line.strip()
        listFromLine = line.split('\t')
        returnMat[index,:] = listFromLine[0:3]	# 选取每行以\t分割的前三个元素,存储到特征矩阵中
        if(listFromLine[-1].isdigit()):			# 将列表的最后一列(目标分类)存储到向量中
            classLabelVector.append(int(listFromLine[-1]))
        else:
            classLabelVector.append(love_dictionary.get(listFromLine[-1]))
        index += 1
    return returnMat,classLabelVector

def testPreDating():
    datingDataMat, datingLabels = file2matrix("/home/hadoop/github-example/machinelearninginaction/Ch02/datingTestSet.txt")
    fig = plt.figure()
    ax = fig.add_subplot(111)
    ax.scatter(datingDataMat[:,1], datingDataMat[:,2], 15.0*array(datingLabels), 15.0*array(datingLabels))
    plt.show()    
    normMat, ranges, minVals = autoNorm(datingDataMat)

# 归一化特征值
def autoNorm(dataSet):
    minVals = dataSet.min(0)
    maxVals = dataSet.max(0)
    ranges = maxVals - minVals
    normDataSet = zeros(shape(dataSet))
    m = dataSet.shape[0]
    normDataSet = dataSet - tile(minVals, (m,1))
    normDataSet = normDataSet/tile(ranges, (m,1))   #element wise divide
    return normDataSet, ranges, minVals
   
# 分类器针对约会网站的测试代码
def datingClassTest():
    hoRatio = 0.50      #hold out 10%
    datingDataMat,datingLabels = file2matrix('datingTestSet2.txt')       #load data set from file
    normMat, ranges, minVals = autoNorm(datingDataMat)
    m = normMat.shape[0]
    numTestVecs = int(m*hoRatio)  # 计算测试向量的数量, 决定了normMat向量中哪些数据用于测试,哪些用于训练
    errorCount = 0.0  # 错误率
    for i in range(numTestVecs):    # 对测试数据集进行分类, 验证测试集的计算结果是否和目标向量相同
        classifierResult = classify0(normMat[i,:],normMat[numTestVecs:m,:],datingLabels[numTestVecs:m],3)
        print "the classifier came back with: %d, the real answer is: %d" % (classifierResult, datingLabels[i])
        if (classifierResult != datingLabels[i]): errorCount += 1.0   # 如果不同, 表示分类器对这个样本计算出错
    print "the total error rate is: %f" % (errorCount/float(numTestVecs))
    print errorCount
    
# 约会网站预测函数
def classifyPerson():
    resultList = ['not at all', 'in small doses', 'in large doses']
    percentTats = float(raw_input(\
                                  "percentage of time spent playing video games?"))
    ffMiles = float(raw_input("frequent flier miles earned per year?"))
    iceCream = float(raw_input("liters of ice cream consumed per year?"))
    datingDataMat, datingLabels = file2matrix('datingTestSet2.txt')
    normMat, ranges, minVals = autoNorm(datingDataMat)
    inArr = array([ffMiles, percentTats, iceCream, ])
    classifierResult = classify0((inArr - \
                                  minVals)/ranges, normMat, datingLabels, 3)
    print "You will probably like this person: %s" % resultList[classifierResult - 1]
    
# 将图像转换为向量
# 把一个32*32的二进制图形转换为1*1024的向量. 一个文件是一个向量
def img2vector(filename):
    returnVect = zeros((1,1024))
    fr = open(filename)
    for i in range(32):		# 循环读出文件的前32行
        lineStr = fr.readline()
        for j in range(32):	# 将每行的头32个字符值存储在数组中
            returnVect[0,32*i+j] = int(lineStr[j])
    return returnVect

# 识别手写数字
def handwritingClassTest():
    hwLabels = []
    trainingFileList = listdir('trainingDigits')           #load the training set获取目录内容
    m = len(trainingFileList)            # 文件数量
    trainingMat = zeros((m,1024))  # 由于一个文件是一个1*1024的向量, m个文件就是m*1024的矩阵
    # 从文件名中解析出分类数字,比如9_45.txt的分类是9
    for i in range(m):
        fileNameStr = trainingFileList[i]
        fileStr = fileNameStr.split('.')[0]         #take off .txt
        classNumStr = int(fileStr.split('_')[0])  #分类数字
        hwLabels.append(classNumStr)        #将分类代码存储在向量中
        trainingMat[i,:] = img2vector('trainingDigits/%s' % fileNameStr)
    # 下一步对testDigits目录中的文件执行类似的操作,不同的是不将这个目录下的文件载入矩阵中
    # 而是使用classify0()函数测试该目录下的每个文件
    testFileList = listdir('testDigits')             #iterate through the test set
    errorCount = 0.0
    mTest = len(testFileList)
    for i in range(mTest):
        fileNameStr = testFileList[i]
        fileStr = fileNameStr.split('.')[0]         #take off .txt
        classNumStr = int(fileStr.split('_')[0])  #分类数字是我们的目标向量
        vectorUnderTest = img2vector('testDigits/%s' % fileNameStr)
        classifierResult = classify0(vectorUnderTest, trainingMat, hwLabels, 3)  # 测试计算结果是否一致
        print "the classifier came back with: %d, the real answer is: %d" % (classifierResult, classNumStr)
        if (classifierResult != classNumStr): errorCount += 1.0
    print "\nthe total number of errors is: %d" % errorCount
    print "\nthe total error rate is: %f" % (errorCount/float(mTest))
