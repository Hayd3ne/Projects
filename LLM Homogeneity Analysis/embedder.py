import sys
from sentence_transformers import SentenceTransformer

model = SentenceTransformer('all-mpnet-base-v2')

if __name__ == "__main__":
    input_path = sys.argv[1]
    output_path = sys.argv[2]

    with open(input_path, "r", encoding="utf-8") as f:
        text = f.read()

    embedding = model.encode(text)

    with open(output_path, "w", encoding="utf-8") as f:
        f.write(str(embedding))


