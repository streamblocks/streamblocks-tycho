network FibsN (N) ==> Out:

entities
	add = [Add() : for i in 2 .. N];

structure
	for i in 0 .. N-2 do
		if i = 0 then
			add[N-2].Result --> add[i].A {initialTokens = [1];} ;
		else
			add[i-1].Result --> add[i].A;
		end
		add[N-2].Result --> add[i].B {initialTokens = [1 : for j in 0..i+1];} ;
	end

	add[N-2].Result --> Out;
end