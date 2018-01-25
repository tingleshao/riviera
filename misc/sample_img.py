from os import listdir
from os.path import isfile, join
from PIL import Image
import random 
import sys
import shutil

# 1. get list of all data.
# 2. compute number of images.
# 3. generate random nubmer 
# 4. based on it select image, copy to new location
def list_files(input_dir):
  files = [f for f in listdir(input_dir) if isfile(join(input_dir, f))]
  return files

def number_of_images(files):
  return len(files)

def select_subset(ratio, files):
  file_size = number_of_images(files)
  indices = range(file_size)
  random.shuffle(indices)
  train_indices = indices[:int(file_size*ratio)]
  eval_indices = indices[int(file_size*ratio):]
  return (train_indices, eval_indices)

def main():
  input_dir = sys.argv[1]
  train_dir = sys.argv[2]
  eval_dir = sys.argv[3]
  files = list_files(input_dir)
  indices = select_subset(0.9, files)
  train_indices = indices[0]
  eval_indices = indices[1]
  for i in train_indices: 
    file_name = input_dir + '/' + files[i]
    shutil.copyfile(file_name, train_dir + '/' + files[i]) 
  for i in eval_indices:
    file_name = input_dir + '/' + files[i]
    shutil.copyfile(file_name, eval_dir + '/' + files[i])


   
if __name__ == '__main__':
  main()
