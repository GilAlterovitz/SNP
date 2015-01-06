import numpy as np


def get_standard(standard_file):
    fp = open(standard_file)
    ret = {}
    for i in fp.readlines():
        s = i.split(' ')
        s = filter(lambda x: x != '', s)
        s[-1] = s[-1][:-1]
        ret[s[0]] = int(s[1]) - 1
    return ret


def read_file(filename):
    fp = open(filename, "r")
    data = []
    header = fp.readline().split('\t')
    header[-1] = header[-1][:-1]
    for line in fp.readlines():
        s = line.split('\t')
        # remove trailing \n
        s[-1] = s[-1][:-1]
        value = s[1:]
        value = [float(i) for i in value]
        data.append(value)
    array = np.array(data)
    array = np.transpose(array)
    return header, array


def read_pca(filename):
    fp = open(filename, "r")
    data = []
    for line in fp.readlines():
        s = line.split('\t')
        s[-1] = s[-1][:-1]
        s = [float(i) for i in s]
        data.append(s)
    array = np.array(data)
    array = np.transpose(array)
    return array


if __name__ == "__main__":
    import sys
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    else:
        filename = "./data/Su_rma_matrix.txt"
    a = read_file(filename)
    print(a)
