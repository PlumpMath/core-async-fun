import itertools
import sys

def print_all(iterable):
    for item in iterable:
        print item

def perms(remainder, prefix):
    if remainder == "":
        yield prefix
    for i in range(0, len(remainder)):
        perms(remainder[:i] + remainder[(i+1):], prefix + remainder[i])

print_all(perms('1234', ''))

def perms(remainder, prefix):
    if remainder == "":
        yield prefix
    for i in range(0, len(remainder)):
        for perm in perms(remainder[:i] + remainder[(i+1):], prefix + remainder[i]):
            yield perm

print_all(perms('1234', ''))

def take10(iterable):
    return itertools.islice(iterable, 10)

def mk_str(size):
    base_str = "1234567890"
    big_str = base_str
    for i in range(size / len(base_str)):
        big_str = big_str + base_str
    return big_str[:size]

big_string = mk_str(100)
super_string = mk_str(1000)

print_all(take10(perms(big_string, '')))

print_all(take10(perms(super_string, '')))

sys.setrecursionlimit(100000)

print_all(take10(perms(super_string, '')))

