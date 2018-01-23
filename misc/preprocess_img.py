from os import listdir
from os.path import isfile, join
from PIL import Image
 
# 1. get list of files
# 2. get the size of each image
# 3. take the shortest dimension, convert to 64. 
# 4. save the new image to another location.
def list_files(input_dir):
  files = [f for f in listdir(input_dir) if isfile(join(input_dir, f))]
  return files


def get_image_size(img_file):
  with Image.open(img_file) as img:
    width, height = img.size
  return (width, height)

def make_new_size(img_size):
  width = img_size[0]
  height = img_size[1]
  if width > height:
    return (width * 64/height,64)
  return (64, height * 64 /width)

def main():
  input_dir = sys.argv[1]
  output_dir = sys.argv[2]
  file_list = list_files(input_dir)
  for img_file in flie_list:
    img_dir = input_dir+'/'+img_file
    old_size = get_image_size(img_dir)
    new_size = make_new_size(old_size)
    with Image.open(img_dir) as img:
        new_img = img.resize(new_size[0], new_size[1], Image.ANTIALIAS)
        new_img.save(output_dir+'/'+img_file)

 
  
