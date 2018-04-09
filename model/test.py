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

graph, graph_def = load_graph("freeze.pb")
#graph = tf.import_graph_def("freeze2.pb", name='prefix')
#with tf.gfile.GFile("freeze2.pb", "wb") as f:
#    f.write(graph_def.SerializeToString())
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
   a = [-2.8830645084381104, 1.180148959159851, 2.8922781944274902, -2.7080748081207275, -1.6551251411437988, 0.7881289720535278, -1.1626136302947998, -1.0747623443603516, -0.422216534614563, 4.462748050689697, 0.866684079170227, -0.8465906381607056, 2.0202553272247314, -0.8157219886779785, 1.2617294788360596, 1.8971368074417114, -0.5761077404022217, -1.907153844833374, 1.4470815658569336, 1.5058015584945679, 1.0783337354660034, 1.3180818557739258, 1.3948237895965576, -1.382979393005371, 0.35344764590263367, -0.08103300631046295, -0.31112441420555115, -0.8658198118209839, -0.41364777088165283, -0.15075089037418365, -2.5181779861450195, 0.2849348485469818, -0.27574336528778076, 1.4179282188415527, -3.0036840438842773, -2.10897159576416, -1.077664852142334, -1.0090718269348145, 0.5800838470458984, -0.9934819936752319, -0.38636288046836853, 1.548470139503479, 4.225962162017822, 0.6225273609161377, -1.9151525497436523, -0.5872492790222168, -1.0458825826644897, 1.60249924659729, -3.533407688140869, 2.429518222808838, 2.075908899307251, -0.4011262357234955, 0.8733375668525696, -1.1757999658584595, -0.5674439668655396, -0.6709281802177429, -1.0302342176437378, 2.4207754135131836, -0.5072906613349915, 0.2827413082122803, -3.28432559967041, -2.286632537841797, -0.7824265360832214, 1.8741588592529297, -1.4021811485290527, -0.015537023544311523, -0.3766331076622009, -2.430584669113159, -1.5975570678710938, 0.8278626799583435, 0.468487024307251, -1.5764023065567017, -0.3413003087043762, 0.8070164918899536, 1.5801687240600586, -0.1829346865415573, 0.1390010118484497, -0.32496967911720276, -2.894637107849121, 0.16400229930877686, -0.6305344700813293, 0.5352773070335388, 0.9189140796661377, 1.0100315809249878, -1.9561344385147095, -2.67404842376709, 0.9511394500732422, -0.20869386196136475, 0.8539302945137024, -1.7760343551635742, -1.6367623805999756, 1.5958318710327148, 1.4736219644546509, 1.2995790243148804, -0.7434713244438171, 0.6129494309425354, 0.8477692008018494, 3.235323429107666, -0.06473882496356964, -0.14247339963912964]
   aa = np.zeros((1,100))
   aa[0] = a
   aaa = sess.run(output_tensor, {'prefix/input:0': aa})
   print aaa
   predictions = tf.image.convert_image_dtype((aaa + 1.0) / 2.0, dtype=tf.uint8, saturate=True)[0]
   print predictions.shape
   print predictions[:,:,0]
   img = Image.fromarray(predictions.eval(), 'RGB')
   img.show()
