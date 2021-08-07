# fr-ml-jslt

My thought process
1. Create a Spring boot app to provide a REST endpoint
2. This endpoint will take input json
3. response json after transformation
4. Create pojos for feature config/transformation

Transformation
1. capture the json input
2. run against the feature transformations to build a response json
3. multiple transformations are possible as its an array for transforms
4. process the trasformations in sequence
5. what is the importance of names in feature configs?
6. Major challenge would to be capture the parent node to replace or fill child elements based on the 
   type of the result
7. Will make some assumptions as the sample output does not provide enough evidence to say what should be the result in case of 
   object/multiple jslts in one transformation

Assumptions:
1. Config server not required 
   would be a good idea for easy change of config for testing transformations
   it would need Service registry as well.
2. will start with basic config with in application as json file
   Chose to do json file instead of string in application properties

