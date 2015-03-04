mapreducepatterns
=================

Repository for MapReduce Design Patterns (O'Reilly 2012) example source code

2-1 Numerical summarizations should be used when both of the following are true:

• You are dealing with numerical data or counting.

• The data can be grouped by specific fields.


2-2 Inverted indexes should be used when

quick search query responses are required.

The results of such a query can be preprocessed and ingested into a database.


2-3 Counting with counters should be used when:

• You have a desire to gather counts or summations over large data sets.

• The number of counters you are going to create is small—in the double digits.


3-1 Filtering is very widely applicable.

The only requirement is that the data can be parsed into “records” that can

be categorized through some well-specified criterion determining whether they are to be kept.


3-2 Bloom Filter

3-3 Top Ten
• This pattern requires a comparator function ability between two records. That is,

  we must be able to compare one record to another to determine which is “larger.”

• The number of output records should be significantly fewer than the number of

  input records because at a certain point it just makes more sense to do a total ordering of the data set.


3-4 Distinct

5-1 A reduce side join should be used when:

• Multiple large data sets are being joined by a foreign key. If all but one of the data

sets can be fit into memory, try using the replicated join.

• You want the flexibility of being able to execute any join operation.


5-2 A replicated join should be used when:

• The type of join to execute is an inner join or a left outer join, with the large input

data set being the “left” part of the operation.

• All of the data sets, except for the large one, can be fit into main memory of each map task.


5-3 A composite join should be used when:

• An inner or full outer join is desired.

• All the data sets are sufficiently large.

• All data sets can be read with the foreign key as the input key to the mapper.

• All data sets have the same number of partitions.

• Each partition is sorted by foreign key, and all the foreign keys reside in the

associated partition of each data set. That is, partition X of data sets A and B contain

the same foreign keys and these foreign keys are present only in partition X.

• The data sets do not change often (if they have to be prepared).