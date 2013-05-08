
network Nats () ==> Out:

entities
  ones = InitialTokens(tokens = [1]);
  sum = Sum();

structure
  ones.Out --> ones.In;
  ones.Out --> sum.In;
  sum.Out --> Out;
end