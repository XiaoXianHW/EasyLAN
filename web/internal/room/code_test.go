package room

import (
	"strings"
	"testing"
)

func TestGenerateCodeShape(t *testing.T) {
	for i := 0; i < 100; i++ {
		c, err := GenerateCode()
		if err != nil {
			t.Fatalf("GenerateCode: %v", err)
		}
		if len(c) != CodeLength+1 {
			t.Fatalf("expected length %d, got %d (%q)", CodeLength+1, len(c), c)
		}
		if c[CodeSeparator] != '-' {
			t.Fatalf("expected '-' at position %d in %q", CodeSeparator, c)
		}
		if err := ValidateCode(c); err != nil {
			t.Fatalf("ValidateCode rejected generated code %q: %v", c, err)
		}
		// alphabet must not include ambiguous characters.
		for _, ch := range c {
			if ch == '-' {
				continue
			}
			if strings.ContainsRune("0OIL1", ch) {
				t.Fatalf("generated code %q contains ambiguous character %q", c, ch)
			}
		}
	}
}

func TestNormalizeCode(t *testing.T) {
	cases := map[string]string{
		" ABCDE-FGHIJ ": "ABCDE-FGHIJ",
		"ABCDEFGHIJ":    "ABCDE-FGHIJ",
		"AB CD EFG HIJ": "ABCDE-FGHIJ",
		"ABCD-E-FGHIJ":  "ABCDE-FGHIJ",
		"abcde-fghij":   "ABCDE-FGHIJ",
	}
	for in, want := range cases {
		if got := NormalizeCode(in); got != want {
			t.Errorf("NormalizeCode(%q) = %q, want %q", in, got, want)
		}
	}
}

func TestValidateCodeRejectsAmbiguous(t *testing.T) {
	bad := []string{
		"00000-00000",
		"AAAAA-AAAAI",
		"AAAAA-AAAA1",
		"OOOOO-OOOOO",
		"ABCDE-FGHI",  // too short
		"ABCDE-FGHIJK", // too long
	}
	for _, b := range bad {
		if err := ValidateCode(b); err == nil {
			t.Errorf("ValidateCode(%q) returned nil; want error", b)
		}
	}
}
