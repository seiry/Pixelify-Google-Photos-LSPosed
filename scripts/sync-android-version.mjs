#!/usr/bin/env node
// Sync release-it's new version into Android-specific source files.
// Invoked by .release-it.json `after:bump` hook, e.g. `node scripts/sync-android-version.mjs 8.1.0`.
//
// What it touches:
//   - app/build.gradle           versionCode += 1, versionName = <new>
//   - update_info.json           latest_version_code = <new versionCode>
//   - strings.xml                <string name="version_head"> = "Version <new>"
//
// version_desc is intentionally NOT touched — curate it manually if you want
// the in-app changelog dialog to show this release's notes. The GitHub Release
// body is auto-generated from CHANGELOG.md regardless.

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { execSync } from 'node:child_process';

const versionName = process.argv[2];
if (!versionName) {
  console.error('Usage: sync-android-version.mjs <versionName>');
  process.exit(1);
}

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(__dirname, '..');

// 1) app/build.gradle — bump versionCode (read + increment) and set versionName.
const gradlePath = path.join(repoRoot, 'app/build.gradle');
let gradle = fs.readFileSync(gradlePath, 'utf-8');

// Match both AGP 8-style "versionCode 10" and AGP 9-style "versionCode = 10".
const codeMatch = gradle.match(/versionCode\s*=?\s*(\d+)/);
if (!codeMatch) {
  throw new Error('Could not find `versionCode <int>` in app/build.gradle');
}
const newCode = parseInt(codeMatch[1], 10) + 1;

gradle = gradle.replace(/versionCode(\s*=?\s*)\d+/, `versionCode$1${newCode}`);
gradle = gradle.replace(/versionName(\s*=?\s*)"[^"]+"/, `versionName$1"${versionName}"`);
fs.writeFileSync(gradlePath, gradle);
console.log(`  ✓ app/build.gradle   versionCode=${newCode}, versionName="${versionName}"`);

// 2) update_info.json — in-app update check reads this from main branch raw URL.
const infoPath = path.join(repoRoot, 'update_info.json');
fs.writeFileSync(
  infoPath,
  JSON.stringify({ latest_version_code: newCode }, null, '\t') + '\n'
);
console.log(`  ✓ update_info.json   latest_version_code=${newCode}`);

// 3) strings.xml — version_head shown in the changelog dialog.
const stringsPath = path.join(repoRoot, 'app/src/main/res/values/strings.xml');
let strings = fs.readFileSync(stringsPath, 'utf-8');
const headRe = /<string name="version_head">[^<]+<\/string>/;
if (!headRe.test(strings)) {
  throw new Error('Could not find <string name="version_head"> in strings.xml');
}
strings = strings.replace(
  headRe,
  `<string name="version_head">Version ${versionName}</string>`
);
fs.writeFileSync(stringsPath, strings);
console.log(`  ✓ strings.xml        version_head="Version ${versionName}"`);

// Stage the touched files so release-it's commit picks them up alongside
// package.json + CHANGELOG.md.
execSync(
  'git add app/build.gradle update_info.json app/src/main/res/values/strings.xml',
  { stdio: 'inherit', cwd: repoRoot }
);

console.log('');
console.log('💡  Tip: edit `version_desc` in strings.xml to refine the in-app');
console.log('    changelog dialog before tagging. The GitHub Release body comes');
console.log('    from CHANGELOG.md (auto-generated from conventional commits).');
