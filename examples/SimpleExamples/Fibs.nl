
network Fibs () ==> Out:

entities
  add = Add();

structure
  add.Result --> add.A {initialTokens = [1];} ;
  add.Result --> add.B {initialTokens = [1, 1];} ;
  add.Result --> Out;
end
