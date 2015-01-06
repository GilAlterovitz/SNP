#!/usr/bin/env python

import data
import itertools
import sys
from sklearn import metrics
from sklearn import decomposition
from matplotlib import pyplot as PLT
from scipy.cluster import hierarchy as hac
from mpl_toolkits.mplot3d import Axes3D


def get_pca(X, n_components):
    """ use pca to do noise filter and decomposition
    Parameters
    ----------
    X: array-like, shape (n_samples, n_features)
        Training vector, where n_samples in the number of samples and
        n_features is the number of features.
    n_components: the dimension of the decomposed array

    Returns
    -------
    X_pca: a decomposed array of X
    """
    n_samples, n_features = X.shape
    X1 = X - X.mean(axis=0)
    X1 -= X1.mean(axis=1).reshape(n_samples, -1)
    pca = decomposition.PCA(n_components=n_components)
    X_pca = pca.fit_transform(X1)
    return X_pca


def hierarchy(X, n_clusters):
    """ apply hierarchy clustering on an array
    Parameters
    ----------
    X: array-like, shape (n_samples, n_features)
        Training vector, where n_samples in the number of samples and
        n_features is the number of features.
    n_clusters: the expected number of clusters to be divided.

    Returns
    -------
    label: an array indicating the cluster that each sample belongs to.
    """
    z = hac.linkage(X, method='ward')
    label = hac.fcluster(z, n_clusters, criterion='maxclust')
    label -= 1
    PLT.show()
    return label


def compare(result, standard):
    """ compare the result and the standard, compute the v measure score
        Parameters
    ----------
    result: labels marked by clustering algorithm
    standard: standard result of clustering

    Returns
    -------
    score: the v measure score of result and standard
    """
    y1 = []
    y2 = []
    for i in result.keys():
        y1.append(standard[i])
        y2.append(int(result[i]))
    score = metrics.v_measure_score(y1, y2)
    return score


def max_match(result, standard, n_clusters):
    """ transform the result array to maximize the matching number to standard
        result
    Parameters
    ----------
    result: labels marked by clustering algorithm
    standard: standard result of clustering
    n_clusters: number of clusters

    Returns
    -------
    score: transformed array of result
    """
    best = -1
    sol = range(n_clusters)
    for it in itertools.permutations(range(n_clusters), n_clusters):
        cnt = 0
        for j in result:
            if it[result[j]] == standard[j]:
                cnt += 1
        if cnt > best or best == -1:
            sol = it
            best = cnt
    for i in result:
        result[i] = sol[result[i]]
    return result


def plot(X, label, n_clusters):
    """
    plot the data with the clustering label
    Parameters
    ----------
    X: array-like, shape (n_samples, n_features)
        Training vector, where n_samples in the number of samples and
        n_features is the number of features.
    label: result of clustering
    n_clusters: number of clusters
    """
    labelkey=label.keys()
    label = label.values()
    data_resc = get_pca(X, 3)
    #clr = ['#20FFFF', '#28A240', '#DFDF00', '#F23072']
    #fig1 = PLT.figure()
    #ax1 = Axes3D(fig1)
    for i in range(len(label)):
        #ax1.scatter(data_resc[i, 0], data_resc[i, 1], data_resc[i, 2],
         #           marker='.', c=clr[label[i]], s=120)
        print "x:{0},y:{1},z:{2},cluster:{3},name:{4}" .format(data_resc[i, 0], data_resc[i, 1], data_resc[i, 2],label[i],labelkey[i])
    #PLT.show()


def work(X, standard, n_clusters, header):
    """ filter the noise of data and cluster the data into several subgroups
    Parameters
    ----------
    X: array-like, shape (n_samples, n_features)
        Training vector, where n_samples in the number of samples and
        n_features is the number of features.
    standard: standard result of clustering
    n_clusters: number of clusters
    header: name of each sample of X
    """
    best_score = 0
    n_samples, n_features = X.shape
    for n_components in range(3, n_samples):
        X_pca = get_pca(X, n_components)
        result = hierarchy(X_pca, n_clusters)
        result2 = dict(zip(header, result))
        result2 = max_match(result2, standard, n_clusters)
        v_measure_score = compare(result2, standard)
        if v_measure_score > best_score:
            best_score = v_measure_score
            best_result = result2

    print best_result
    plot(X, best_result, n_clusters)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    else:
        filename = "./data/Su_mas5_matrix.txt"
    standard = data.get_standard('standard.txt')
    header, X = data.read_file(filename)
    work(X, standard, 4, header)
    standard = data.get_standard('standard3.txt')
    work(X, standard, 3, header)
