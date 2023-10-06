package net.imglib2.brno_learnathon.s1_study_imglib2_basics;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;

public class t3_MovingWithinImage {

	public static <T extends RealType<T>>
	void readAndSetSomePixels(final Img<T> image) {
		//This is the most ideal setting:
		//This is a method that gets a "normal image" as a parameter
		//and does some work on its pixels. For example, it visits all
		//pixels and increases their value by 5:
		for (T pixel : image) pixel.setReal( pixel.getRealDouble() + 5 );
		//or
		image.forEach(pixel -> pixel.setReal( pixel.getRealDouble() + 5 ));
		//or, with an explicit iterating "cursor":
		Cursor<T> cursor = image.cursor();
		while (cursor.hasNext()) {
			T pixel = cursor.next();
			pixel.setReal( pixel.getRealDouble() + 5 );
		}
		//(Notice that the cursor is initialized ill and
		// must be next() before the actual use)
		//
		//Also notice that we didn't have to even think about the actual dimensionality
		//of the image that is being swept. The cursor just visited all pixels in
		//the image and made sure no pixel was accessed twice; but(!) the order in
		//which the pixels were visited is not defined -- every image backend has
		//the choice to iterate in an order that's optimal for it
		moreOnIteratingUsingCursors(image);


		//On the other hand, to a) deal with the actual pixel positions explicitly
		//and b) to have a full control over which pixel will be accessed now,
		//some(!) images offer random position "accessors":
		RandomAccess<T> accessor = image.randomAccess();

		//The accessor by default points "to nowhere", to an illegal image position,
		//it must be always set before the actual use:
		int[] coordinate = new int[ image.numDimensions() ]; //Java autofills with zeros
		accessor.setPosition( coordinate );
		//
		//or set position in only one dimension (here the 3rd dimension):
		accessor.setPosition( coordinate[2], 2 );
		//
		//after positioned, follows the usual work with pixel type:
		accessor.get().getRealDouble();

		//This positions and fetches the pixel in one call, and then fetches the value:
		accessor.setPositionAndGet( coordinate ).getRealDouble();

		//Besides (re)setting the accessor position (as shown above),
		//one can ask to have it advanced along the given dimension.
		//But beware, no image bounds are checked.
		accessor.fwd(0);
		accessor.fwd(1);
		accessor.fwd(1);
		accessor.fwd(2);

		//Where is the accessor now?
		accessor.localize( coordinate );

		//This is another convenient shortcut.. if one knows the dimensionality:
		if (image.numDimensions() == 5) accessor.setPositionAndGet(0,0,0,0,0);

		//Btw, there exists even a shorter shortcut ;)....:
		image.getAt( coordinate ).getRealDouble();
		//But it internally always(!) creates a new RandomAccessor,
		//so use this only "safely", e.g., not in loops.
	}

	public static <T extends RealType<T>>
	void moreOnIteratingUsingCursors(final Img<T> image) {
		//While we (normally) cannot influence the iteration order,
		//we still query the position where the cursor currently is.
		int[] coordinate = new int[ image.numDimensions() ];

		//There are in fact two cursors available:
		Cursor<T> fasterMovingCursor = image.cursor();
		Cursor<T> positionAwareCursor = image.localizingCursor();
		//They both visit the image, in the same order etc.
		//The only difference is that the latter is doing some extra
		//housekeeping (which takes a tiny bit more of time) to be able
		//to faster answer where it is currently positioned within the image.
		//The former can still tell, but the query is more expensive.
		int moves = 6;
		while (fasterMovingCursor.hasNext() && moves > 0) {
			fasterMovingCursor.next();
			moves--;
		}
		fasterMovingCursor.localize( coordinate );
		System.out.println(" (Normal) Cursor reports position: "+ Util.printCoordinates(coordinate));
		//
		moves = 6;
		while (positionAwareCursor.hasNext() && moves > 0) {
			positionAwareCursor.next();
			moves--;
		}
		positionAwareCursor.localize( coordinate );
		System.out.println("localizingCursor reports position: "+ Util.printCoordinates(coordinate));

		//If for whatever reason one needs to "bookmark" the current position
		//during the image sweeping, one can copy() the cursor and pass it to
		//a specialized related method -- the cursor also knows/remembers all
		//it needs in order to conduct the sweep, the reference on the original
		//image itself is not needed for the sweep.
		Cursor<T> anotherSuchCursor = positionAwareCursor.copy();
		continueScanningTheImageFromHere(anotherSuchCursor);
		//(good catch! The function doesn't really need a localizingCursor, I know...)
		//
		//Show that the original cursor hasn't changed but the copy did change:
		positionAwareCursor.localize( coordinate );
		System.out.println("original Cursor reports position: "+ Util.printCoordinates(coordinate));
		anotherSuchCursor.localize( coordinate );
		System.out.println("  cloned Cursor reports position: "+ Util.printCoordinates(coordinate));
	}

	public static <T extends RealType<T>>
	void continueScanningTheImageFromHere(final Cursor<T> cursor) {
		//Just run to the very end of the image.
		while (cursor.hasNext()) cursor.next();
	}

	public static void main(String[] args) {
		final Img<UnsignedShortType> gray16Image = PlanarImgs.unsignedShorts(200,200,3);
		readAndSetSomePixels( gray16Image );
	}
}
