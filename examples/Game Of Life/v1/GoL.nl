
network GoL (w, h, init, nSteps) ==> Display:

var
	bufSz = 3;

entities

	a = [
			if i = 0 or i > w then
				[Edge() : for j in 0..w + 1]
			else
				[
			    	if j = 0 or j > w then
			    		Edge()
			    	else 
			    		Cell(init = init[i-1][j-1], x = i-1, y = j-1, nSteps = nSteps)
			    	end
			    	: for j in 0 .. w + 1
			    ]
			end
			: for i in 0 .. h + 1
		];
		
structure
	
	foreach i in 1..h, foreach j in 1..w do
		a[i+1][j+1].Out --> a[i][j].SE {bufferSize = bufSz;} ;
		a[i+1][j].Out --> a[i][j].S {bufferSize = bufSz;} ;
		a[i+1][j-1].Out --> a[i][j].SW {bufferSize = bufSz;} ;
		a[i][j+1].Out --> a[i][j].E {bufferSize = bufSz;} ;
		a[i][j-1].Out --> a[i][j].W {bufferSize = bufSz;} ;
		a[i-1][j+1].Out --> a[i][j].NE {bufferSize = bufSz;} ;
		a[i-1][j].Out --> a[i][j].N {bufferSize = bufSz;} ;
		a[i-1][j-1].Out --> a[i][j].NW {bufferSize = bufSz;} ;
		
		a[i][j].Display --> Display;
	end
end


