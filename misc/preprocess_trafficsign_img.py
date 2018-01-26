import sys
from os import listdir
from os.path import isfile, join
from PIL import Image
from os.path import isdir
 
# 1. get list of files
# 2. get the size of each image
# 3. take the shortest dimension, convert to 64. 
# 4. save the new image to another location.
def list_dirs(input_dir):
  dirs = [d for d in listdir(input_dir) if isdir(join(input_dir, d))]
  return dirs 

def list_files(input_dir):
  files = [f for f in listdir(input_dir) if isfile(join(input_dir, f)) and f != '.DS_Store' and f.split('.')[1] == 'ppm']
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

def is_to_small(img_size):
  if img_size[0] < 30 or img_size[1] < 30:
    return True
  return False

def main():
  input_dir = sys.argv[1]
  output_dir = sys.argv[2]
  dir_list = list_dirs(input_dir)
  for d in dir_list:
    file_list = list_files(input_dir+ '/' + d)
    for img_file in file_list:
      img_dir = input_dir+'/' + d + '/' +img_file
      old_size = get_image_size(img_dir)
      if not is_to_small(old_size):
        new_size = make_new_size(old_size)
        with Image.open(img_dir) as img:
          new_img = img.resize(new_size, Image.ANTIALIAS)
          new_img.save(output_dir + '/' + d + '_' + img_file.split('.')[0] + '.jpg')
  
if __name__ == "__main__":
  main()
