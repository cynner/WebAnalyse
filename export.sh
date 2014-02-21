#!/bin/bash
CODEDIR=$1
CODETGZ="${CODEDIR}.tgz"
TARGETSITE='thanaphol@gpu.mikelab.net'
TARGETDIR='/san00/mike/boom/proj'
CMDAFTER="tar -xzvf $CODETGZ"
tar -czvf $CODETGZ $CODEDIR
scp $CODETGZ $TARGETSITE:$TARGETDIR
ssh $TARGETSITE "cd $TARGETDIR; $CMDAFTER"
