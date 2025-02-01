from setuptools import setup, find_packages
import os

def load_requirements(filename='requirements.txt'):
    """Load requirements from a file, ignoring comments and blank lines."""
    with open(filename, 'r', encoding='utf-8') as req_file:
        lines = req_file.readlines()

    # Filter out comments and empty lines
    requirements = []
    for line in lines:
        line = line.strip()
        if line and not line.startswith('#'):
            requirements.append(line)
    return requirements

setup(
    name='springbit-ml',
    version='1.0.0',
    packages=find_packages(),
    install_requires=load_requirements(),
    author='glypher',
    author_email='admin@springbit.org',
    description='ML package for springbit',
    long_description=open('README.md', encoding='utf-8').read() if os.path.exists('README.md') else '',
    long_description_content_type='text/markdown',
    url='https://github.com/glypher/spring-bit/tree/main/spring-bit-ml',
    classifiers=[
        'Programming Language :: Python :: 3',
        'Operating System :: OS Independent',
    ],
    python_requires='>=3.6',
)
