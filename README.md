# TextNormalizer
This is a separated and improved improvement of MARK Stemmer. The first paper it was introduced is: Mining User Opinions in Mobile App Reviews: A Keyword-Based Approach. (https://arxiv.org/pdf/1505.04657.pdf)

The spell corrector needs a corpus to learn the significancy of words it has. And Github doesn't let me upload a big file so you must provide your own text corpus. Any english text corpus will do. You can find its location in the corrector code.

This project includes a dictionary of English root words and their variations. Those words were collected from many sources (including wordnet, online IT dictionaries, linux dictionary and my own findings in mobile app reviews), most of which were cited in my paper, except for Wordnet because this is a newer version and I haven't published any paper with this version yet. The words were manually checked, given roots and proper form for their variations. To date, it may be the most complete root-variations English dictionary out there that include IT/mordern/slang terms that is available.

If you decided to use this work of mine, please cite the following paper:
Vu, Phong Minh, et al. "Mining User Opinions in Mobile App Reviews: A Keyword-Based Approach (T)." Automated Software Engineering (ASE), 2015 30th IEEE/ACM International Conference on. IEEE, 2015.

