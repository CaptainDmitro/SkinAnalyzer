# SkinAnalyzer
An android application to classify skin lesions:
- Nevus;
- Melanoma;
- Benign keratosis;
- Basal cell carcinom;
- Actinic keratoses;
- Vascular skin lesions;
- Dermatofibroma.

|<video src='https://user-images.githubusercontent.com/48180766/181248055-655aefae-9aac-430a-8af0-aecabcebafea.mov'/>|![image](https://user-images.githubusercontent.com/48180766/181247432-940ede9a-f765-43fb-8e44-1bcd9ffa54bc.png)|![image](https://user-images.githubusercontent.com/48180766/181247612-676292ed-caed-4a3a-8f02-5a0ed2fc98e1.png)|![image](https://user-images.githubusercontent.com/48180766/181247794-f1f0afd3-ea62-405f-89a3-9cfd6231ddf5.png)|
|--------------|-----------|------------|----------|

# Model
EfficientNet based model trained on merged data from HAM1000 and ISIC2019.
It was trained on 50 000 images (augmented data included).
|              | Precision |   Recall   | F1-score | Support |
|--------------|-----------|------------|----------|---------|
|    akiec     |   0.64    |    0.52    |   0.57   |    31   |                 
|    bcc       |   0.75    |    0.78    |   0.75   |    32   |
|    bkl     |   0.74    |    0.69    |   0.69   |    80   |
|    df     |   0.66    |    0.64    |   0.64   |    9   |
|    mel     |   0.48    |    0.66    |   0.55   |    44   |
|    nv     |   0.97    |    0.97    |   0.97   |    779   |
|    vasc     |   0.93    |    0.93    |   0.93   |    32   |
|--------------|-----------|------------|----------|---------|
|    avg     |   0.75    |    0.74    |   0.74   |    957   |
|    weighted avg     |   0.92    |    0.91    |   0.91   |    957   |

- Recall shows if the classifier is able to detect a class;
- Precision shows if the classifier is able to detect a class correctly;
- F1 Score is a harmonic mean of the recall and precision. In common words: the higher - the better.

In case of android the model was converted to tflite with merged metadata with [TensorFlow Lite metadata script](https://www.tensorflow.org/lite/models/convert/metadata#examples "Adding metadata to TensorFlow lite models").
