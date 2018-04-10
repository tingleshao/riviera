import tensorflow as tf
import os
import numpy as np
from tensorflow.python.platform import gfile
from PIL import Image
from google.protobuf import text_format


data = np.arange(10,dtype=np.int32)

def load_graph(frozen_graph_filename):
    # We load the protobuf file from the disk and parse it to retrieve the
    # unserialized graph_def
    with tf.gfile.GFile(frozen_graph_filename, "rb") as f:
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(f.read())

    for node in graph_def.node:
            if node.op == 'RefSwitch':
                node.op = 'Switch'
                for index in xrange(len(node.input)):
                    if 'moving_' in node.input[index]:
                        node.input[index] = node.input[index] + '/read'
            elif node.op == 'AssignSub':
                node.op = 'Sub'
                if 'use_locking' in node.attr: del node.attr['use_locking']
            elif node.op == 'AssignAdd':
                node.op = 'Add'
                if 'use_locking' in node.attr: del node.attr['use_locking']

    # Then, we import the graph_def into a new Graph and returns it
    with tf.Graph().as_default() as graph:
        # The name var will prefix every op/nodes in your graph
        # Since we load everything in a new graph, this is not needed
        tf.import_graph_def(graph_def, name="prefix")
    return graph, graph_def

graph, graph_def = load_graph("freeze_bird_10_embed_to_image.pb")
#graph = tf.import_graph_def("freeze2.pb", name='prefix')
with tf.gfile.GFile("freeze_bird_10_embed_to_image2.pb", "wb") as f:
    f.write(graph_def.SerializeToString())
#tf.train.write_graph(graph, '', 'freeze2.pb')
for op in graph.get_operations():
        print(op.name)

with tf.Session(graph=graph) as sess:
# with tf.Session(graph=tf.Graph()) as sess:
#    tf.saved_model.loader.load(
#        sess,
#        [tf.saved_model.tag_constants.SERVING],
#        "/Users/chongshao/dev/riviera/model/generative_chongshao_20180121_131920_image_in/")
   output_tensor = sess.graph.get_tensor_by_name('prefix/gen_deconv3/Tanh:0')
   a = [-4.183666706085205, -2.4661941528320312, 1.257184624671936, -4.537239074707031, 5.261890888214111, 7.5772786140441895, -0.798807680606842, -7.147511005401611, 3.960893154144287, 5.556200981140137]
   aa = np.zeros((1,10))
   aa[0] = a
   aaa = sess.run(output_tensor, {'prefix/input:0': aa})
   print aaa
   predictions = tf.image.convert_image_dtype((aaa + 1.0) / 2.0, dtype=tf.uint8, saturate=True)[0]
   print predictions.shape
   print predictions[:,:,0]
   img = Image.fromarray(predictions.eval(), 'RGB')
   img.show()
