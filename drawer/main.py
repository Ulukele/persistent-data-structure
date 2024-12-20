import matplotlib.pyplot as plt
import numpy as np
import sys

def read_times_from_file(filename):
    with open(filename, 'r') as file:
        numbers = [[int(t.strip()) for t in (line.strip().split())] for line in file if line.strip()]
    return np.rot90(np.array(numbers))

first_algo = sys.argv[1]
second_algo = sys.argv[2]

first_res = read_times_from_file(first_algo)
second_res = read_times_from_file(second_algo)

operations = ('update', 'read', 'insert')

plt.figure(figsize=(12, 6))
plots = 3
for i in range(plots):
    plt.subplot(1, plots, i + 1)
    plt.plot(first_res[i], label='first', marker='o')
    plt.plot(second_res[i], label='second', marker='x')
    plt.title(operations[i])
    plt.grid()
plt.show()
