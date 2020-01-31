{ pkgs ? import <nixpkgs> {}
, sbtOptions ? ""

, stdenv ? pkgs.stdenv
, makeWrapper ? pkgs.makeWrapper
, jdk ? pkgs.jdk
, sbt ? pkgs.sbt

}: with pkgs;

stdenv.mkDerivation rec {
  buildInputs = [ makeWrapper jdk sbt ];
  name = "zinc-bw-patched";
  version = "0.3.13-bw1";
  src = ./.;

  # Fixed-output - build is deterministic, and once it's in the cache we don't care
  # if upstream libs become unavailable or if workers need to download them for the
  # initial build INTO the cache.
  outputHashMode = "recursive";
  outputHashAlgo = "sha256";
  outputHash = "0kcdzz9hl07nq9d4b3mfxcifbh32akd9vb4k89j10in54drdmazm";

  dontPatchELF      = true;
  dontStrip         = true;

  # set environment variable to affect all SBT commands, keeping
  # the build as hermetic as possible.
  SBT_OPTS = ''
    -Dsbt.ivy.home=./.ivy2/
    -Dsbt.boot.directory=./.sbt/boot/
    -Dsbt.global.base=./.sbt
    -Dsbt.global.staging=./.staging
    ${sbtOptions}
  '';
    # Removed from above list, but present in sbtix build command:
    # -Dsbt.override.build.repos=true
    # -Dsbt.repository.config=${sbtixRepos}

  buildPhase = ''pwd && sbt compile'';

  installPhase = ''
    sbt stage
    mkdir -p $out/

    # Drag along a copy of the unmodified (but renamed) zinc wrapper script for windows use.  Do this
    # by copying the script and marking it non-executable, so it's skipped by the nix shell patcher.
    cp target/universal/stage/bin/zinc target/universal/stage/bin/zinc.no_shebang_patch_for_windows
    chmod -x target/universal/stage/bin/zinc.no_shebang_patch_for_windows

    cp target/universal/stage/* $out/ -r
  '';
}
