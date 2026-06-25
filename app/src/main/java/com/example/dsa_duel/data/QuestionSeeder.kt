package com.example.dsa_duel.data

/**
 * FILE: data/QuestionSeeder.kt
 *
 * The complete DSA question bank for DSA Duel.
 *
 * Contains 50 questions across 9 topics:
 *   - Arrays        (8 questions)
 *   - LinkedList    (6 questions)
 *   - Strings       (6 questions)
 *   - Stacks        (5 questions)
 *   - Trees         (6 questions)
 *   - Sorting       (5 questions)
 *   - BinarySearch  (5 questions)
 *   - Graphs        (5 questions)
 *   - DP            (4 questions)
 *
 * Each question has:
 *   - 4 MCQ options (A, B, C, D)
 *   - 1 correct answer
 *   - explanation shown after duel
 *   - ELO range (who sees this question)
 *   - time limit in seconds
 *
 * HOW TO ADD MORE QUESTIONS:
 * Copy any existing QuestionEntity block, increment the id,
 * change the content, and add it to the list.
 * The id must be unique — no two questions can share an id.
 */
object QuestionSeeder {

    val questions: List<QuestionEntity> = listOf(

        // ══════════════════════════════════════════════════════════
        // ARRAYS  (id: 1 - 8)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 1,
            title = "Two Sum",
            description = "Given an array of integers and a target, " +
                    "return the best approach to find two numbers that add up to target.",
            topic = "Arrays",
            difficulty = "EASY",
            optionA = "Nested loops — O(n²) time, O(1) space",
            optionB = "HashMap — O(n) time, O(n) space",
            optionC = "Sort then binary search — O(n log n) time",
            optionD = "Recursive approach — O(2ⁿ) time",
            correctAnswer = "B",
            explanation = "HashMap stores each number as key and its index as value. " +
                    "For each element x, check if (target - x) already exists in the map. " +
                    "Single pass → O(n) time, O(n) space. Most optimal solution.",
            minElo = 800, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 2,
            title = "Find Maximum Subarray",
            description = "Which algorithm finds the maximum sum contiguous " +
                    "subarray in O(n) time?",
            topic = "Arrays",
            difficulty = "EASY",
            optionA = "Binary Search",
            optionB = "Merge Sort approach",
            optionC = "Kadane's Algorithm",
            optionD = "Sliding Window",
            correctAnswer = "C",
            explanation = "Kadane's Algorithm iterates once, maintaining currentMax " +
                    "and globalMax. At each step: currentMax = max(element, currentMax + element). " +
                    "If currentMax < 0, reset to 0. Time: O(n), Space: O(1).",
            minElo = 800, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 3,
            title = "Move Zeroes",
            description = "Move all zeroes to end of array while maintaining " +
                    "relative order of non-zero elements. Best approach?",
            topic = "Arrays",
            difficulty = "EASY",
            optionA = "Create new array — O(n) space",
            optionB = "Two pointer in-place — O(n) time O(1) space",
            optionC = "Sort the array",
            optionD = "Use a Stack",
            correctAnswer = "B",
            explanation = "Two pointer: left pointer tracks position for next non-zero, " +
                    "right pointer scans. When right finds non-zero, swap with left position " +
                    "and advance left. In-place, single pass, stable order maintained.",
            minElo = 800, maxElo = 1300, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 4,
            title = "Find Duplicate Number",
            description = "Array of n+1 integers, values 1 to n. Find duplicate " +
                    "in O(n) time WITHOUT modifying array and O(1) extra space.",
            topic = "Arrays",
            difficulty = "MEDIUM",
            optionA = "Sort the array and check adjacent elements",
            optionB = "Use a HashSet to track seen numbers",
            optionC = "Floyd's Cycle Detection (fast & slow pointers)",
            optionD = "XOR all elements with 1 to n",
            correctAnswer = "C",
            explanation = "Treat array values as next pointers → forms a linked list " +
                    "with a cycle. Floyd's algorithm finds cycle start = duplicate. " +
                    "O(n) time, O(1) space — only solution meeting all constraints.",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 5,
            title = "Container With Most Water",
            description = "Array represents heights of walls. Find two walls " +
                    "that together with x-axis contain the most water.",
            topic = "Arrays",
            difficulty = "MEDIUM",
            optionA = "Brute force all pairs — O(n²)",
            optionB = "Two pointers from both ends — O(n)",
            optionC = "Stack-based approach — O(n)",
            optionD = "Dynamic programming — O(n²)",
            correctAnswer = "B",
            explanation = "Place left pointer at start, right at end. " +
                    "Area = min(height[l], height[r]) × (r - l). " +
                    "Move the pointer with smaller height inward — this is the " +
                    "only way to potentially find a larger area. O(n) one pass.",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 6,
            title = "Rotate Array",
            description = "Rotate array of n elements to the right by k steps " +
                    "in O(1) extra space.",
            topic = "Arrays",
            difficulty = "MEDIUM",
            optionA = "Shift elements one by one k times — O(n×k)",
            optionB = "Use extra array — O(n) space",
            optionC = "Three reversal trick — O(n) time O(1) space",
            optionD = "Recursive rotation",
            correctAnswer = "C",
            explanation = "Three reversals: (1) reverse entire array, " +
                    "(2) reverse first k elements, (3) reverse remaining n-k elements. " +
                    "Example: [1,2,3,4,5], k=2 → [5,4,3,2,1] → [4,5,3,2,1] → [4,5,1,2,3].",
            minElo = 1000, maxElo = 1500, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 7,
            title = "Product of Array Except Self",
            description = "Return array where output[i] = product of all elements " +
                    "EXCEPT self. No division allowed, O(n) time.",
            topic = "Arrays",
            difficulty = "HARD",
            optionA = "Calculate total product then divide",
            optionB = "Left and right prefix product arrays",
            optionC = "Nested loops for each element",
            optionD = "Logarithms to convert division to subtraction",
            correctAnswer = "B",
            explanation = "Build left prefix products (product of all elements before i) " +
                    "and right suffix products (product of all elements after i). " +
                    "output[i] = left[i] × right[i]. O(n) time, O(n) space (or O(1) with optimization).",
            minElo = 1400, maxElo = 2000, timeLimitSeconds = 60
        ),

        QuestionEntity(
            id = 8,
            title = "Trapping Rain Water",
            description = "Given elevation map, compute how much water it can trap " +
                    "after raining. What is the optimal time and space complexity?",
            topic = "Arrays",
            difficulty = "HARD",
            optionA = "O(n²) time, O(1) space using brute force",
            optionB = "O(n) time, O(n) space using prefix max arrays",
            optionC = "O(n) time, O(1) space using two pointers",
            optionD = "O(n log n) time using sorting",
            correctAnswer = "C",
            explanation = "Two pointer approach: maintain leftMax and rightMax. " +
                    "If leftMax < rightMax: water at left = leftMax - height[left], move left. " +
                    "Else: water at right = rightMax - height[right], move right. " +
                    "Single pass, constant space — most optimal solution.",
            minElo = 1500, maxElo = 2200, timeLimitSeconds = 60
        ),

        // ══════════════════════════════════════════════════════════
        // LINKED LIST  (id: 9 - 14)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 9,
            title = "Reverse a Linked List",
            description = "Reverse a singly linked list. What is the " +
                    "optimal approach?",
            topic = "LinkedList",
            difficulty = "EASY",
            optionA = "Recursive — O(n) time, O(n) stack space",
            optionB = "Iterative with three pointers — O(n) time, O(1) space",
            optionC = "Store in array then rebuild — O(n) space",
            optionD = "Swap node values — O(n²) time",
            correctAnswer = "B",
            explanation = "Iterative: maintain prev=null, curr=head, next=null. " +
                    "Loop: next=curr.next, curr.next=prev, prev=curr, curr=next. " +
                    "When curr==null, prev is the new head. O(n) time, O(1) space.",
            minElo = 900, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 10,
            title = "Detect Cycle in Linked List",
            description = "Detect if a linked list has a cycle. " +
                    "Most space-efficient approach?",
            topic = "LinkedList",
            difficulty = "EASY",
            optionA = "HashSet to store visited nodes — O(n) space",
            optionB = "Mark visited nodes by modifying values",
            optionC = "Floyd's Cycle Detection — slow & fast pointers",
            optionD = "Count nodes and compare with expected length",
            correctAnswer = "C",
            explanation = "Floyd's: slow moves 1 step, fast moves 2 steps. " +
                    "If cycle exists, they WILL meet inside the cycle. " +
                    "If fast reaches null, no cycle. O(n) time, O(1) space — " +
                    "no extra memory needed unlike HashSet approach.",
            minElo = 900, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 11,
            title = "Merge Two Sorted Lists",
            description = "Merge two sorted linked lists into one sorted list. " +
                    "Best approach?",
            topic = "LinkedList",
            difficulty = "EASY",
            optionA = "Collect all nodes in array, sort, rebuild",
            optionB = "Iterative with dummy head node",
            optionC = "Recursive merge",
            optionD = "Both B and C are equally optimal",
            correctAnswer = "D",
            explanation = "Both iterative (dummy head) and recursive approaches " +
                    "run in O(m+n) time. Iterative uses O(1) extra space. " +
                    "Recursive uses O(m+n) stack space. In interviews, " +
                    "iterative with dummy head is preferred for space efficiency.",
            minElo = 900, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 12,
            title = "Find Middle of Linked List",
            description = "Find the middle node of a linked list in one pass " +
                    "without knowing the length.",
            topic = "LinkedList",
            difficulty = "EASY",
            optionA = "Count nodes, then traverse to n/2",
            optionB = "Store all nodes in array, return middle index",
            optionC = "Slow and fast pointer — fast moves 2x speed",
            optionD = "Recursive approach",
            correctAnswer = "C",
            explanation = "Slow pointer moves 1 step, fast pointer moves 2 steps. " +
                    "When fast reaches end, slow is at middle. " +
                    "Single pass O(n), O(1) space. For even-length lists, " +
                    "slow stops at second middle node.",
            minElo = 800, maxElo = 1300, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 13,
            title = "LRU Cache Implementation",
            description = "Implement LRU Cache with O(1) get and put. " +
                    "Which data structure combination achieves this?",
            topic = "LinkedList",
            difficulty = "HARD",
            optionA = "Array + Linear search",
            optionB = "HashMap only",
            optionC = "HashMap + Doubly Linked List",
            optionD = "TreeMap for ordered access",
            correctAnswer = "C",
            explanation = "HashMap gives O(1) key lookup. Doubly Linked List " +
                    "gives O(1) insertion/deletion at both ends. " +
                    "HashMap stores key→node reference, DLL maintains usage order. " +
                    "On get: move node to front. On put: add to front, evict from back if full.",
            minElo = 1400, maxElo = 2100, timeLimitSeconds = 60
        ),

        QuestionEntity(
            id = 14,
            title = "Remove Nth Node From End",
            description = "Remove the nth node from the end of a linked list " +
                    "in ONE pass.",
            topic = "LinkedList",
            difficulty = "MEDIUM",
            optionA = "Two passes — first count length, second remove",
            optionB = "Two pointers with n-gap between them",
            optionC = "Stack to track order",
            optionD = "Recursive backtracking",
            correctAnswer = "B",
            explanation = "Two pointers both start at dummy head. " +
                    "Advance fast pointer n+1 steps ahead. " +
                    "Move both until fast hits null. " +
                    "Slow is now at node BEFORE the target. " +
                    "slow.next = slow.next.next removes the target. O(n) single pass.",
            minElo = 1000, maxElo = 1600, timeLimitSeconds = 90
        ),

        // ══════════════════════════════════════════════════════════
        // STRINGS  (id: 15 - 20)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 15,
            title = "Valid Anagram",
            description = "Check if two strings are anagrams of each other. " +
                    "Most efficient approach?",
            topic = "Strings",
            difficulty = "EASY",
            optionA = "Sort both strings and compare — O(n log n)",
            optionB = "Character frequency array of size 26 — O(n)",
            optionC = "HashMap for character counts — O(n)",
            optionD = "Both B and C are O(n) but B is better",
            correctAnswer = "D",
            explanation = "Both use O(n) time. The int[26] array approach uses " +
                    "exactly O(26) = O(1) space for lowercase English letters. " +
                    "HashMap uses more memory due to boxing overhead. " +
                    "For fixed alphabet, array is always preferred over HashMap.",
            minElo = 800, maxElo = 1300, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 16,
            title = "Longest Substring Without Repeating",
            description = "Find length of longest substring without repeating " +
                    "characters. Optimal approach?",
            topic = "Strings",
            difficulty = "MEDIUM",
            optionA = "Generate all substrings — O(n³)",
            optionB = "Sliding window with HashSet — O(n)",
            optionC = "Dynamic programming — O(n²)",
            optionD = "Two pointer with sorting",
            correctAnswer = "B",
            explanation = "Sliding window: expand right pointer, add chars to HashSet. " +
                    "When duplicate found, shrink from left until duplicate removed. " +
                    "Track max window size throughout. O(n) time, O(min(m,n)) space " +
                    "where m = charset size.",
            minElo = 1000, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 17,
            title = "Valid Palindrome",
            description = "Check if a string is a palindrome considering only " +
                    "alphanumeric characters, ignoring cases.",
            topic = "Strings",
            difficulty = "EASY",
            optionA = "Reverse string and compare — O(n) space",
            optionB = "Two pointers from both ends — O(1) space",
            optionC = "Stack to check character order",
            optionD = "Regex to clean then reverse",
            correctAnswer = "B",
            explanation = "Left pointer starts at 0, right at end. " +
                    "Skip non-alphanumeric characters on both sides. " +
                    "Compare lowercase of both characters. " +
                    "If mismatch found → not palindrome. O(n) time, O(1) space.",
            minElo = 800, maxElo = 1300, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 18,
            title = "String to Integer (atoi)",
            description = "Implement atoi which converts a string to a 32-bit " +
                    "signed integer. What must you handle?",
            topic = "Strings",
            difficulty = "MEDIUM",
            optionA = "Only digits, ignore everything else",
            optionB = "Leading whitespace, optional sign, digits, overflow",
            optionC = "Just parse digit by digit",
            optionD = "Use built-in parseInt directly",
            correctAnswer = "B",
            explanation = "Correct atoi must handle: " +
                    "(1) Skip leading whitespace, " +
                    "(2) Optional '+'/'-' sign, " +
                    "(3) Read digits until non-digit, " +
                    "(4) Clamp to INT_MIN/INT_MAX on overflow. " +
                    "Overflow check: if result > INT_MAX/10, it will overflow before multiplying.",
            minElo = 1000, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 19,
            title = "Longest Common Prefix",
            description = "Find the longest common prefix among an array of strings.",
            topic = "Strings",
            difficulty = "EASY",
            optionA = "Sort array, compare first and last strings only",
            optionB = "Vertical scanning — compare column by column",
            optionC = "Horizontal scanning — reduce prefix one string at a time",
            optionD = "Both A and C are correct approaches",
            correctAnswer = "D",
            explanation = "All three approaches work. Sort + compare first/last is elegant: " +
                    "after sorting, first and last strings are most different, " +
                    "so their common prefix is the answer for all strings. " +
                    "O(n log n) for sorting. Horizontal scanning is O(n×m) where m = prefix length.",
            minElo = 800, maxElo = 1300, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 20,
            title = "Group Anagrams",
            description = "Group strings that are anagrams of each other. " +
                    "What is the key insight for grouping?",
            topic = "Strings",
            difficulty = "MEDIUM",
            optionA = "Compare every string with every other — O(n²)",
            optionB = "Sort each string to create a canonical key for HashMap",
            optionC = "Use character frequency array as HashMap key",
            optionD = "Both B and C work — C is slightly faster",
            correctAnswer = "D",
            explanation = "B: Sort each string → sorted string is the key. O(n × k log k). " +
                    "C: Count char frequencies → '#a2#b1...' style key. O(n × k). " +
                    "C is asymptotically faster since O(k) < O(k log k). " +
                    "Both use HashMap<String, List<String>> to group anagrams.",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        // ══════════════════════════════════════════════════════════
        // STACKS  (id: 21 - 25)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 21,
            title = "Valid Parentheses",
            description = "Given string of brackets '()[]{}'.  " +
                    "Check if brackets are valid and properly closed.",
            topic = "Stacks",
            difficulty = "EASY",
            optionA = "Count open and close brackets",
            optionB = "Stack — push open brackets, pop on close",
            optionC = "Recursion",
            optionD = "Replace pairs iteratively until empty",
            correctAnswer = "B",
            explanation = "Stack approach: for each char, if opening bracket push to stack. " +
                    "If closing bracket, check if stack top is matching opener. " +
                    "If yes pop, if no return false. After processing, " +
                    "stack must be empty for valid string. O(n) time, O(n) space.",
            minElo = 900, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 22,
            title = "Min Stack",
            description = "Design a stack that supports push, pop, top, and " +
                    "getMin in O(1) time.",
            topic = "Stacks",
            difficulty = "MEDIUM",
            optionA = "Use one stack, scan all elements for min — O(n) getMin",
            optionB = "Two stacks — main stack and min-tracking stack",
            optionC = "Sort stack on every push",
            optionD = "Store min as a class variable",
            correctAnswer = "B",
            explanation = "Two stacks: main stack stores all values, " +
                    "minStack stores current minimum at each state. " +
                    "On push(x): push to main. If x <= minStack.top(), push to minStack. " +
                    "On pop: if popped value == minStack.top(), pop minStack too. " +
                    "getMin() always returns minStack.top(). All O(1).",
            minElo = 1000, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 23,
            title = "Daily Temperatures",
            description = "Find how many days until a warmer temperature for each day. " +
                    "Optimal approach?",
            topic = "Stacks",
            difficulty = "MEDIUM",
            optionA = "Brute force — for each day check all future days O(n²)",
            optionB = "Monotonic decreasing stack storing indices",
            optionC = "Binary search on future temperatures",
            optionD = "DP from right to left",
            correctAnswer = "B",
            explanation = "Monotonic stack: push index onto stack. " +
                    "For each new temperature, while stack not empty AND " +
                    "temperatures[stack.top] < current temperature: " +
                    "pop index, answer[index] = current_index - index. " +
                    "Push current index. O(n) time — each index pushed and popped once.",
            minElo = 1100, maxElo = 1700, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 24,
            title = "Evaluate Reverse Polish Notation",
            description = "Evaluate arithmetic expression in Reverse Polish Notation " +
                    "(postfix). Which data structure is ideal?",
            topic = "Stacks",
            difficulty = "MEDIUM",
            optionA = "Queue — process tokens in order",
            optionB = "Stack — push operands, pop on operator",
            optionC = "Recursion with operator precedence",
            optionD = "Convert to infix first then evaluate",
            correctAnswer = "B",
            explanation = "Stack: for each token, if number push to stack. " +
                    "If operator (+,-,*,/), pop top two numbers, apply operator, " +
                    "push result back. After processing all tokens, " +
                    "stack contains the final answer. O(n) time, O(n) space.",
            minElo = 1000, maxElo = 1500, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 25,
            title = "Largest Rectangle in Histogram",
            description = "Find the largest rectangle that can be formed in a histogram. " +
                    "What is the optimal time complexity?",
            topic = "Stacks",
            difficulty = "HARD",
            optionA = "O(n²) checking every pair of bars",
            optionB = "O(n log n) divide and conquer",
            optionC = "O(n) using monotonic increasing stack",
            optionD = "O(n) using two pointer approach",
            correctAnswer = "C",
            explanation = "Monotonic stack stores indices of bars in increasing height order. " +
                    "When a shorter bar is found, pop taller bars and calculate their max rectangle. " +
                    "Width = current_index - stack.top - 1. " +
                    "Each bar is pushed and popped once → O(n) total. Most optimal.",
            minElo = 1500, maxElo = 2200, timeLimitSeconds = 60
        ),

        // ══════════════════════════════════════════════════════════
        // TREES  (id: 26 - 31)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 26,
            title = "Maximum Depth of Binary Tree",
            description = "Find the maximum depth (height) of a binary tree.",
            topic = "Trees",
            difficulty = "EASY",
            optionA = "BFS level-order traversal — count levels",
            optionB = "DFS recursive — max(left, right) + 1",
            optionC = "Iterative DFS using stack",
            optionD = "All three give same result and same complexity",
            correctAnswer = "D",
            explanation = "All approaches are O(n) time, O(h) space where h = height. " +
                    "Recursive DFS: return max(depth(left), depth(right)) + 1. " +
                    "BFS: count number of levels processed. " +
                    "Iterative DFS: track max depth in stack entries. " +
                    "Recursive is shortest code — preferred in interviews.",
            minElo = 900, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 27,
            title = "Validate Binary Search Tree",
            description = "Determine if a binary tree is a valid BST.",
            topic = "Trees",
            difficulty = "MEDIUM",
            optionA = "Check if left < root < right for each node locally",
            optionB = "Inorder traversal should give sorted sequence",
            optionC = "Pass min/max bounds recursively to each node",
            optionD = "Both B and C are correct",
            correctAnswer = "D",
            explanation = "A: WRONG — checking locally fails for nodes deeper in tree. " +
                    "B: Inorder traversal of valid BST gives strictly increasing sequence. " +
                    "C: Pass bounds — each node must be within (min, max). " +
                    "Left subtree gets (min, root.val), right gets (root.val, max). " +
                    "Both B and C are valid O(n) solutions.",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 28,
            title = "Lowest Common Ancestor",
            description = "Find the Lowest Common Ancestor (LCA) of two nodes " +
                    "in a Binary Search Tree.",
            topic = "Trees",
            difficulty = "MEDIUM",
            optionA = "Store paths from root to each node, find last common node",
            optionB = "Use BST property — navigate left or right based on values",
            optionC = "BFS from both nodes simultaneously",
            optionD = "Recursive DFS checking all subtrees",
            correctAnswer = "B",
            explanation = "BST LCA uses BST property: " +
                    "If both p and q are less than current node → LCA is in left subtree. " +
                    "If both greater → LCA is in right subtree. " +
                    "If they split (one left, one right) → current node IS the LCA. " +
                    "O(h) time where h = tree height. O(log n) for balanced BST.",
            minElo = 1100, maxElo = 1700, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 29,
            title = "Level Order Traversal",
            description = "Return level-by-level values of a binary tree " +
                    "(BFS order). Which data structure drives this?",
            topic = "Trees",
            difficulty = "EASY",
            optionA = "Stack — LIFO order",
            optionB = "Queue — FIFO order",
            optionC = "Priority Queue — by node value",
            optionD = "Recursion with level parameter",
            correctAnswer = "B",
            explanation = "Queue (FIFO) naturally processes nodes level by level. " +
                    "Start: enqueue root. Each iteration: dequeue node, " +
                    "add its value to current level, enqueue left and right children. " +
                    "Track level size to separate levels. O(n) time and space.",
            minElo = 900, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 30,
            title = "Diameter of Binary Tree",
            description = "Find the length of the longest path between any two " +
                    "nodes in a binary tree.",
            topic = "Trees",
            difficulty = "MEDIUM",
            optionA = "BFS from each leaf to find longest path — O(n²)",
            optionB = "Single DFS — diameter at each node = leftHeight + rightHeight",
            optionC = "Find two farthest leaves iteratively",
            optionD = "DP storing diameter for each subtree",
            correctAnswer = "B",
            explanation = "Single DFS postorder traversal. At each node, " +
                    "diameter passing through it = leftHeight + rightHeight. " +
                    "Update global max diameter. Return height = max(left, right) + 1 to parent. " +
                    "The path doesn't need to pass through root. O(n) time, O(h) space.",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 31,
            title = "Serialize and Deserialize Binary Tree",
            description = "Design an algorithm to serialize a binary tree to string " +
                    "and deserialize it back. What traversal works best?",
            topic = "Trees",
            difficulty = "HARD",
            optionA = "Inorder traversal — sufficient for reconstruction",
            optionB = "Preorder traversal with null markers",
            optionC = "Level order BFS with null markers",
            optionD = "Both B and C work correctly",
            correctAnswer = "D",
            explanation = "Inorder alone is NOT sufficient — can't uniquely reconstruct. " +
                    "Preorder with null markers: visit node, then serialize left, then right. " +
                    "Null nodes marked as '#'. During deserialize, use queue of tokens. " +
                    "BFS with null markers also works. Both are O(n) time and space.",
            minElo = 1500, maxElo = 2200, timeLimitSeconds = 60
        ),

        // ══════════════════════════════════════════════════════════
        // SORTING  (id: 32 - 36)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 32,
            title = "Best Sorting Algorithm",
            description = "For large datasets with no prior information about data, " +
                    "which sorting algorithm is generally the best choice?",
            topic = "Sorting",
            difficulty = "EASY",
            optionA = "Bubble Sort — simple to implement",
            optionB = "Merge Sort — guaranteed O(n log n) worst case",
            optionC = "Quick Sort — O(n log n) average, widely used in practice",
            optionD = "Insertion Sort — adaptive for nearly sorted data",
            correctAnswer = "C",
            explanation = "QuickSort dominates in practice due to cache efficiency " +
                    "and low constant factors. Average O(n log n), worst O(n²) " +
                    "but rare with good pivot selection (random or median-of-3). " +
                    "Most language standard libraries use Introsort (QuickSort + HeapSort hybrid).",
            minElo = 900, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 33,
            title = "Merge Sort Space Complexity",
            description = "What is the space complexity of the standard " +
                    "top-down Merge Sort implementation?",
            topic = "Sorting",
            difficulty = "EASY",
            optionA = "O(1) — sorts in place",
            optionB = "O(log n) — only recursion stack",
            optionC = "O(n) — auxiliary array for merging",
            optionD = "O(n log n) — new array at each level",
            correctAnswer = "C",
            explanation = "Merge Sort needs O(n) auxiliary space for the temporary " +
                    "array used during the merge step. The recursion stack adds O(log n) " +
                    "but O(n) dominates. Total: O(n). " +
                    "This is why QuickSort is preferred when memory is a concern.",
            minElo = 900, maxElo = 1400, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 34,
            title = "Sort Colors (Dutch National Flag)",
            description = "Sort array containing only 0s, 1s, and 2s in-place " +
                    "in O(n) time and O(1) space.",
            topic = "Sorting",
            difficulty = "MEDIUM",
            optionA = "Count 0s, 1s, 2s then fill array",
            optionB = "Dutch National Flag — three pointer approach",
            optionC = "Quick sort with custom comparator",
            optionD = "Both A and B satisfy constraints",
            correctAnswer = "D",
            explanation = "A: Two-pass O(n) — count then fill. Simple but two passes. " +
                    "B: Dutch National Flag by Dijkstra — single pass O(n). " +
                    "Three pointers: low=0 (boundary of 0s), mid=0 (current), high=n-1 (boundary of 2s). " +
                    "If arr[mid]=0: swap with low, advance both. " +
                    "If arr[mid]=1: advance mid. If arr[mid]=2: swap with high, retreat high.",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 35,
            title = "Kth Largest Element",
            description = "Find the kth largest element in an unsorted array. " +
                    "What is the optimal average time complexity?",
            topic = "Sorting",
            difficulty = "MEDIUM",
            optionA = "Sort array and return index n-k — O(n log n)",
            optionB = "Min-heap of size k — O(n log k)",
            optionC = "QuickSelect algorithm — O(n) average",
            optionD = "Both B and C, C is better",
            correctAnswer = "D",
            explanation = "B: Min-heap of size k: push elements, pop when size > k. " +
                    "Top of heap is kth largest. O(n log k). Good for streaming data. " +
                    "C: QuickSelect — partition-based like QuickSort but only recurse on one side. " +
                    "O(n) average, O(n²) worst. For interview: both are acceptable, " +
                    "QuickSelect is optimal average.",
            minElo = 1200, maxElo = 1700, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 36,
            title = "Counting Sort Use Case",
            description = "When is Counting Sort more efficient than comparison-based sorts?",
            topic = "Sorting",
            difficulty = "MEDIUM",
            optionA = "When the array is already nearly sorted",
            optionB = "When elements are integers in a small known range",
            optionC = "When the array has many duplicates",
            optionD = "When sorting strings",
            correctAnswer = "B",
            explanation = "Counting Sort is O(n + k) where k = range of values. " +
                    "It's only efficient when k = O(n), i.e., range is proportional to array size. " +
                    "Example: sort 1 million numbers all between 1-1000 → O(n+1000) = O(n). " +
                    "For large ranges (e.g., sorting ages 0-120 in array of 100), k dominates and wastes space.",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        // ══════════════════════════════════════════════════════════
        // BINARY SEARCH  (id: 37 - 41)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 37,
            title = "Binary Search Prerequisite",
            description = "What is the fundamental prerequisite for applying " +
                    "Binary Search to an array?",
            topic = "BinarySearch",
            difficulty = "EASY",
            optionA = "Array must contain only integers",
            optionB = "Array must be sorted",
            optionC = "Array must have odd number of elements",
            optionD = "Array must not have duplicates",
            correctAnswer = "B",
            explanation = "Binary Search requires the search space to be SORTED " +
                    "(or have a monotonic property). Each comparison eliminates half the " +
                    "search space — this only works when elements are in order. " +
                    "Without sorting, mid comparison gives no information about which half to discard.",
            minElo = 800, maxElo = 1300, timeLimitSeconds = 120
        ),

        QuestionEntity(
            id = 38,
            title = "Search in Rotated Sorted Array",
            description = "Search for a target in a sorted array that has been " +
                    "rotated at an unknown pivot. Time complexity?",
            topic = "BinarySearch",
            difficulty = "MEDIUM",
            optionA = "O(n) — linear search since array is rotated",
            optionB = "O(log n) — modified binary search",
            optionC = "O(log² n) — binary search twice",
            optionD = "O(n log n) — sort then binary search",
            correctAnswer = "B",
            explanation = "Modified binary search: at each step, one half is always sorted. " +
                    "Check if left half sorted: if nums[left] <= nums[mid]. " +
                    "If target in sorted half → search there. Else search other half. " +
                    "Each step eliminates half → O(log n) maintained.",
            minElo = 1100, maxElo = 1700, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 39,
            title = "Find Peak Element",
            description = "A peak element is greater than its neighbors. " +
                    "Find any peak element. What is optimal complexity?",
            topic = "BinarySearch",
            difficulty = "MEDIUM",
            optionA = "O(n) — scan entire array",
            optionB = "O(log n) — binary search on slope direction",
            optionC = "O(1) — always at the middle",
            optionD = "O(n log n) — sort to find maximum",
            correctAnswer = "B",
            explanation = "Binary search on slope: check mid vs mid+1. " +
                    "If nums[mid] < nums[mid+1] → peak is in right half (going uphill). " +
                    "If nums[mid] > nums[mid+1] → peak is in left half including mid. " +
                    "Key insight: if going uphill, a peak MUST exist in that direction. " +
                    "O(log n) guaranteed.",
            minElo = 1200, maxElo = 1700, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 40,
            title = "Sqrt(x) Without Math Library",
            description = "Compute integer square root of x without using " +
                    "built-in sqrt functions. Optimal approach?",
            topic = "BinarySearch",
            difficulty = "MEDIUM",
            optionA = "Linear search from 0 to x — O(√x)",
            optionB = "Binary search between 0 and x — O(log x)",
            optionC = "Newton's method — O(log x)",
            optionD = "Both B and C are O(log x)",
            correctAnswer = "D",
            explanation = "Binary search: search space 0 to x. " +
                    "If mid * mid == x → return mid. " +
                    "If mid * mid < x → answer might be mid, search right. " +
                    "If mid * mid > x → search left. Return right at end. " +
                    "Newton's method: x_new = (x_old + n/x_old) / 2. Both O(log x).",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 41,
            title = "Median of Two Sorted Arrays",
            description = "Find the median of two sorted arrays of sizes m and n. " +
                    "What is the optimal time complexity?",
            topic = "BinarySearch",
            difficulty = "HARD",
            optionA = "O(m+n) — merge then find median",
            optionB = "O(log(m+n)) — binary search on partition",
            optionC = "O(log(min(m,n))) — binary search on smaller array",
            optionD = "O(n log n) — sort combined arrays",
            correctAnswer = "C",
            explanation = "Binary search on the SMALLER array. " +
                    "Partition both arrays such that left half total = right half total. " +
                    "Adjust partition until max(left sides) <= min(right sides). " +
                    "O(log(min(m,n))) — only binary search on smaller array. " +
                    "This is the hardest binary search problem — common in FAANG interviews.",
            minElo = 1600, maxElo = 2500, timeLimitSeconds = 60
        ),

        // ══════════════════════════════════════════════════════════
        // GRAPHS  (id: 42 - 46)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 42,
            title = "BFS vs DFS — When to Use Which",
            description = "You need to find the SHORTEST PATH in an unweighted graph. " +
                    "Which traversal should you use?",
            topic = "Graphs",
            difficulty = "EASY",
            optionA = "DFS — explores deep paths quickly",
            optionB = "BFS — explores level by level guaranteeing shortest path",
            optionC = "Either works for unweighted graphs",
            optionD = "Dijkstra's — even for unweighted",
            correctAnswer = "B",
            explanation = "BFS explores nodes level by level. In unweighted graphs, " +
                    "each level = one more edge from source. First time BFS reaches " +
                    "destination = guaranteed shortest path. " +
                    "DFS might find a path but not necessarily shortest. " +
                    "Dijkstra = BFS with weights — overkill for unweighted.",
            minElo = 1100, maxElo = 1600, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 43,
            title = "Detect Cycle in Directed Graph",
            description = "How do you detect a cycle in a directed graph?",
            topic = "Graphs",
            difficulty = "MEDIUM",
            optionA = "BFS from every node",
            optionB = "DFS with visited set only",
            optionC = "DFS with visited + recursion stack (in-progress) tracking",
            optionD = "Union-Find / Disjoint Set",
            correctAnswer = "C",
            explanation = "For DIRECTED graphs, you need two states: " +
                    "visited (fully processed) and in-stack (currently in DFS path). " +
                    "If DFS encounters a node in current recursion stack → cycle! " +
                    "Just 'visited' set is enough for undirected graphs but NOT directed. " +
                    "Union-Find works for undirected graphs efficiently.",
            minElo = 1300, maxElo = 1800, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 44,
            title = "Number of Islands",
            description = "Count the number of islands in a 2D grid of '1's (land) " +
                    "and '0's (water).",
            topic = "Graphs",
            difficulty = "MEDIUM",
            optionA = "Count all '1' cells",
            optionB = "DFS/BFS from each unvisited '1', marking connected cells",
            optionC = "Dynamic programming",
            optionD = "Sort cells and group connected ones",
            correctAnswer = "B",
            explanation = "Treat grid as graph — each cell is a node, " +
                    "adjacent land cells are edges. DFS/BFS from every unvisited '1': " +
                    "mark all connected '1's as visited (sink them to '0'). " +
                    "Each DFS call = one island found. Count DFS calls. " +
                    "Time: O(m×n), Space: O(m×n) recursion stack.",
            minElo = 1100, maxElo = 1700, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 45,
            title = "Dijkstra's Algorithm Use Case",
            description = "Dijkstra's algorithm finds shortest path. What is " +
                    "its key constraint?",
            topic = "Graphs",
            difficulty = "MEDIUM",
            optionA = "Only works on directed graphs",
            optionB = "Only works on unweighted graphs",
            optionC = "Does not work with negative edge weights",
            optionD = "Requires the graph to be a tree",
            correctAnswer = "C",
            explanation = "Dijkstra's greedy assumption: once a node is finalized, " +
                    "its shortest distance won't decrease. NEGATIVE edges break this — " +
                    "a future negative edge could provide a shorter path to an already-finalized node. " +
                    "For negative weights → use Bellman-Ford (O(VE)) or SPFA. " +
                    "Dijkstra with min-heap: O((V+E) log V).",
            minElo = 1300, maxElo = 1900, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 46,
            title = "Topological Sort",
            description = "Topological sort is used to order nodes of a directed " +
                    "acyclic graph. Which algorithm is NOT suitable for this?",
            topic = "Graphs",
            difficulty = "HARD",
            optionA = "DFS with finish-time ordering (reverse postorder)",
            optionB = "Kahn's algorithm (BFS with in-degree)",
            optionC = "Dijkstra's shortest path algorithm",
            optionD = "Both A and B work correctly",
            correctAnswer = "C",
            explanation = "Topological sort: valid for DAGs (no cycles). " +
                    "DFS approach: run DFS, add node to stack AFTER processing all neighbors. " +
                    "Reverse the stack for topological order. " +
                    "Kahn's: track in-degrees, process nodes with in-degree 0 first via queue. " +
                    "Dijkstra finds shortest paths — completely different problem, wrong choice here.",
            minElo = 1400, maxElo = 2000, timeLimitSeconds = 60
        ),

        // ══════════════════════════════════════════════════════════
        // DYNAMIC PROGRAMMING  (id: 47 - 50)
        // ══════════════════════════════════════════════════════════

        QuestionEntity(
            id = 47,
            title = "Fibonacci — Top Down vs Bottom Up",
            description = "Compute nth Fibonacci number efficiently. " +
                    "What distinguishes memoization from tabulation?",
            topic = "DP",
            difficulty = "EASY",
            optionA = "Memoization = bottom-up, Tabulation = top-down",
            optionB = "Memoization = top-down recursion + cache, Tabulation = bottom-up iteration",
            optionC = "They are the same approach with different names",
            optionD = "Memoization is always faster than tabulation",
            correctAnswer = "B",
            explanation = "Memoization (Top-Down): start from fib(n), recurse down, " +
                    "cache results to avoid recomputation. Uses recursion stack. " +
                    "Tabulation (Bottom-Up): build dp array from dp[0] up to dp[n]. " +
                    "Iterative, no recursion stack. Both are O(n) time. " +
                    "Tabulation often has better constant factor (no function call overhead).",
            minElo = 1000, maxElo = 1500, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 48,
            title = "0/1 Knapsack Problem",
            description = "Classic 0/1 Knapsack: n items, each with weight and value. " +
                    "Maximize value within weight capacity W. Time complexity?",
            topic = "DP",
            difficulty = "MEDIUM",
            optionA = "O(n) — single pass through items",
            optionB = "O(n × W) — 2D DP table",
            optionC = "O(2ⁿ) — try all combinations",
            optionD = "O(n log W) — binary search optimization",
            correctAnswer = "B",
            explanation = "Build 2D dp table: dp[i][w] = max value using first i items with capacity w. " +
                    "Recurrence: dp[i][w] = max(dp[i-1][w], dp[i-1][w-weight[i]] + value[i]). " +
                    "Fill n rows × W columns → O(n×W) time and space. " +
                    "Space optimizable to O(W) using 1D array traversed right-to-left.",
            minElo = 1200, maxElo = 1800, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 49,
            title = "Longest Common Subsequence",
            description = "Find the length of the Longest Common Subsequence (LCS) " +
                    "of two strings of length m and n.",
            topic = "DP",
            difficulty = "MEDIUM",
            optionA = "O(m+n) — single pass comparison",
            optionB = "O(m×n) — 2D DP table",
            optionC = "O(m×n×min(m,n)) — recursive with multiple states",
            optionD = "O(n log n) — patience sorting approach",
            correctAnswer = "B",
            explanation = "2D DP: dp[i][j] = LCS length of first i chars of s1 and j chars of s2. " +
                    "If s1[i] == s2[j]: dp[i][j] = dp[i-1][j-1] + 1. " +
                    "Else: dp[i][j] = max(dp[i-1][j], dp[i][j-1]). " +
                    "Fill m×n table → O(m×n) time and space. " +
                    "Note: LCS ≠ substring. Subsequence doesn't require consecutive characters.",
            minElo = 1300, maxElo = 1900, timeLimitSeconds = 90
        ),

        QuestionEntity(
            id = 50,
            title = "Coin Change Problem",
            description = "Given coin denominations and amount, find minimum " +
                    "number of coins to make the amount. Approach?",
            topic = "DP",
            difficulty = "MEDIUM",
            optionA = "Greedy — always pick largest coin first",
            optionB = "Bottom-up DP — dp[amount] = min coins needed",
            optionC = "BFS — amount as graph node, coins as edges",
            optionD = "Both B and C are correct, A is not always correct",
            correctAnswer = "D",
            explanation = "A (Greedy) FAILS for some inputs. Example: coins=[1,3,4], amount=6. " +
                    "Greedy: 4+1+1=3 coins. DP: 3+3=2 coins. Greedy is wrong! " +
                    "B (DP): dp[i] = min(dp[i - coin] + 1) for each coin. O(amount × coins). " +
                    "C (BFS): treat amount as graph, each coin is an edge. BFS finds minimum steps. " +
                    "Same complexity as DP. Both B and C are valid correct solutions.",
            minElo = 1200, maxElo = 1800, timeLimitSeconds = 90
        )
    )
}