import tensorflow as tf
import os
import numpy as np
from tensorflow.python.platform import gfile

data = np.arange(10,dtype=np.int32)

import tensorflow as tf

with tf.Session(graph=tf.Graph()) as sess:
   tf.saved_model.loader.load(
       sess,
       [tf.saved_model.tag_constants.SERVING],
       "/Users/chongshao/dev/riviera/model/generative_chongshao_20180121_131920/")

# TODO:
# 1. read generate_img.sh
# 2. find the place to make prediction
# 3. copy the code here.
