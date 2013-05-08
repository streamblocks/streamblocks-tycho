
network SDkbest (k, N, R, O) Y ==> S:

var 
	w = #O;
	
	function partialError (i) :
		lambda (choices, Y) :
			square(sum([choices[i] * R[i] : for i in 0 .. #choices - 1]) - Y[i])
		end
	end
	
	function better(v, w) :
		v["error"] < w["error"]
	end

    function inSelect(n) :
    	if n = 0 then
    		w
    	else
    		w * outSelect(n-1)
    	end
    end
    
    function outSelect(n) :
    	min(k, inSelect(n))
    end

entities

    e = [ [Expand(choice = o, errorf = partialError(i)) : for o in O] :
          for i in 0..N-1];

	s = [ Select(N = inSelect(i), K = if i = N-1 then 1 else outSelect(i) end, better = better) :
	      for i in 0 .. N-1];
	      	
	stripS = StripS();

structure

	for i in 0 .. N-1 do
		for j in 0 .. w-1 do
			if i = 0 then
				Y --> e[i][j].In;
			else
				s[i - 1].Out --> e[i][j].In;
			end
			e[i][j].Out --> s[i].In;
			s[i].Ack --> e[i][j].Next;
		end
	end
	
	s[N-1].Out --> stripS.V;
	stripS.S --> S;
end

