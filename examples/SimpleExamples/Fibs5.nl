network Fibs5 () ==> Out:

entities
  init1 = InitialTokens(tokens = [1]);
  init2 = InitialTokens(tokens = [1]);
  init3 = InitialTokens(tokens = [1]);
  init4 = InitialTokens(tokens = [1]);
  init5 = InitialTokens(tokens = [1]);
  add1 = Add();
  add2 = Add();
  add3 = Add();
  add4 = Add();

structure
  init1.Out --> init2.In;
  init2.Out --> init3.In;
  init3.Out --> init4.In;
  init4.Out --> init5.In;

  init1.Out --> add1.A;
  init2.Out --> add1.B;

  add1.Result --> add2.A;
  init3.Out --> add2.B;

  add2.Result --> add3.A;
  init4.Out --> add3.B;

  add3.Result --> add4.A;
  init5.Out --> add4.B;

  add4.Result --> init1.In;
  add4.Result --> Out;
end

