# zip an experiment
zipExp() {
	FILENAME=$1.zip
	zip -r $1.zip Evaluations/$1
	mv $1.zip Evaluations_zipped/
}

mkdir -p Evaluations_zipped
export -f zipExp
ls Evaluations | cat | parallel --jobs 16 --bar zipExp {} 


