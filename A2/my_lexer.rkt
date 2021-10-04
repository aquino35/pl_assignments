#lang racket
;; Osvaldo Aquino - Programming Languages - Racket Lexer Assignment

(require parser-tools/lex)
(require (prefix-in : parser-tools/lex-sre))

(define my_lexer_input_port
  (lexer
   [(:+ (:or (char-range #\a #\z) (char-range #\A #\Z))) ;; identifiers
    (cons `(ID ,(string->symbol lexeme))
          (my_lexer input-port))]

   ;; delimeters:
   [#\(
    (cons'(LP)
          (my_lexer input-port))]
   
   [#\)
    (cons'(RP) 
          (my_lexer input-port))]

   [#\[
    (cons'(LB)
          (my_lexer input-port))]
   
   [#\]
    (cons'(RB) 
          (my_lexer input-port))]

   ;; operators:
   [(:or #\+ #\*)
    (cons`(OP ,(string->symbol lexeme))
          (my_lexer input-port))]

    [#\%
    (cons`(OP ,(string->symbol "module"))
          (my_lexer input-port))]

   ;; integers:
   [(:: (:? #\-) (:+ (char-range #\0 #\9)))
    (cons`(INT ,(string->number lexeme))
          (my_lexer input-port))]
   
   ;; whitespace:
   [whitespace 
    (my_lexer input-port)]
   
   [(eof)
    '()]))

(define (my_lexer_string string)
  (my_lexer_input_port (open-input-string string)))

(define (my_lexer input)
  (if (string? input)
      (my_lexer_string input)
      (my_lexer_input_port input)))