# Postmortem: v1.3 iteration

Kept in the repository so the tutor can read it later. This is about the process, not the product.

## What worked

- The iteration was delivered and released inside the deadline. Tag `v1.3` at `5149f1d5`, `proptrack.jar`
  at 12.53 MB, release notes naming what is new against AB3, nine screenshots taken from the released
  jar, milestone closed at 23:01, 58 minutes before Thursday 23:59.
- Every feature went in through a reviewed PR, with an issue, a milestone and a label. Fourteen items
  closed in the iteration, and the merges were tested in a scratch clone before they were merged.
- The release notes described behaviour rather than effort, and the screenshots were captured from the
  built jar rather than from a mock-up.

## What did not work

- **A correctness bug shipped in v1.3.** Creating an appointment did not create an undo step, so undo
  erased it together with the client edit before it, redo stayed armed and could overwrite a new
  appointment, and an older snapshot could bring back an appointment the user had been told was deleted.
  PR #74 fixed all of it in v1.4.
- **The process cause, which matters more than the bug.** Committing a checkpoint was each mutator's own
  responsibility, so an appointment path added by a later feature silently skipped it. The merge that
  combined undo with appointments was signed off on a green run with 368 tests passing, and no test
  combined the two features. A green suite told us nothing had regressed, not that anything was covered.
- **Nobody had written down what undo should do to appointments.** The feature document and the user
  guide described the two features separately, so there was no rule for a reviewer to measure against.
- **CI stopped running on 14 October**, and we found out only when PRs showed no checks.
- **Reviews were approvals, not reviews.** Two inline comments in the whole iteration, both on the v1.1
  photo PR, and the review comments that did exist sat in the PR thread rather than on the code.
- **Commit bodies were not wrapped at 72 characters** in 7 of 121 commits. Nothing else in the messages
  broke a convention.
- **A new issue was filed against the closed v1.3 milestone**, so v1.3 briefly showed an open item and
  the issue had to be moved to v1.4.
- **One documentation edit rewrote a whole guide's line endings**, turning a one-sentence change into a
  644-line diff. It was reverted and redone, but it cost an hour.

## Process changes adopted for v1.4 and v1.5

1. **Write the interaction rule before the code.** For every feature still to come, the history, both
   filters, the saved file and the failure path are written down, plus the acceptance test that proves
   it. A feature does not start until its rule exists.
2. **Review with inline comments on the code**, two or three substantive ones per PR, instead of a bare
   approval or a thread reply. Only comments placed on code lines are counted as review contributions.
3. **Treat a merge between a data-changing feature and the history feature as unresolved** until the
   mutation path has been read and one cross-feature scenario has run through the real parser.
4. **Wrap commit bodies, and re-read the message** with `git log -1 --format=%B` before pushing.
   Subjects stay imperative, capitalised and inside 50 characters.
5. **Keep milestone discipline.** New work goes to the open milestone only, and a closed milestone stays
   at zero open items.
6. **One feature per branch, opened as a parallel PR early**, with a minimal version first and
   improvements as follow-up PRs.
7. **Verify every release from a pinned commit and read the asset back.**

## Outcome

The measures held through v1.4 and v1.5: restore, backup and the data-file messages were built from
written rules, and the alpha test found defects in the product rather than in the process.
